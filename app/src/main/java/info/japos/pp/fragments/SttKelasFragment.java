package info.japos.pp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.activities.StatistikActivity;
import info.japos.pp.adapters.SttJadwalViewAdapter;
import info.japos.pp.bus.BusStation;
import info.japos.pp.bus.events.FragmentResumedEvent;
import info.japos.pp.bus.events.UserDomainChangedEvent;
import info.japos.pp.models.kbm.common.ItemSectionInterface;
import info.japos.pp.models.kbm.common.SectionGroupTitle;
import info.japos.pp.models.kbm.kelas.Kelas;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.pp.retrofit.KelasService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.EqualSpacingItemDecoration;
import info.japos.utils.RecyclerColumnQty;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SttKelasFragment extends Fragment implements View.OnClickListener, OnItemSelected, SwipeRefreshLayout.OnRefreshListener, DatePickerDialog.OnDateSetListener {

    private static String TAG = SttKelasFragment.class.getSimpleName();

    private int userDomainId;

    private static final String STATE_USERDOMAIN_ID = "userDomainId";

    private ArrayList<ItemSectionInterface> mKelasAndSectionList = new ArrayList<>();
    private SttJadwalViewAdapter sttViewAdapter;
    private Call<List<Kelas>> mCallKelas;
    private boolean isFirstLoad = Boolean.TRUE;
    private Calendar datePicker = Calendar.getInstance();
    private Calendar datePickerEnd = Calendar.getInstance();
    private SharedPreferences sharedpreferences;

    @BindView(R.id.et_periode) EditText periode;
    @BindView(R.id.rv_kelas) RecyclerView kelasView;
    @BindView(R.id.tv_no_result) TextView noResultInfo;
    @BindView(R.id.btn_submit_pp) Button btnNext;
    @BindView(R.id.swipe_refresh_kelas) SwipeRefreshLayout swipeRefreshKelas;

    public SttKelasFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistik, container,false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get bundle params
        Bundle args = getArguments();
        if (args != null) userDomainId = args.getInt("UserDomainId");
    }

    @Override
    public void onResume() {
        super.onResume();

        // register bus
        BusStation.getBus().register(this);

        // sent event
        new Handler().postDelayed(() -> BusStation.getBus().post(new FragmentResumedEvent()), 500);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(Boolean.FALSE);

        // butter knife binding
        ButterKnife.bind(this, view);

        // shared preferences
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivityNonNull());

        // bind event onclick to this
        periode.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        swipeRefreshKelas.setOnRefreshListener(this);

        // swipe refresh
        swipeRefreshKelas.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.BLUE);

        // RecyclerView
        RecyclerColumnQty recyclerColumnQty = new RecyclerColumnQty(kelasView.getContext(), R.layout.item_kelas);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(kelasView.getContext(),recyclerColumnQty.calculateNoOfColumns());
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return SttJadwalViewAdapter.SECTION_VIEW == sttViewAdapter.getItemViewType(position) ? recyclerColumnQty.calculateNoOfColumns() : 1;
            }
        });

        kelasView.addItemDecoration(new EqualSpacingItemDecoration(12, EqualSpacingItemDecoration.GRID)); // 8px. In practice, you'll want to use getDimensionPixelSize
        kelasView.setLayoutManager(gridLayoutManager);
        sttViewAdapter = new SttJadwalViewAdapter(getContext(), this, mKelasAndSectionList);
        kelasView.setAdapter(sttViewAdapter);

        initBundleHandler(savedInstanceState);

        // set tanggalkbm text view
        showLabelDatePicker(datePicker, datePickerEnd);
        periode.setInputType(InputType.TYPE_NULL);

        // post runnable to run fetching data
        swipeRefreshKelas.postDelayed(() -> getKelasList(datePicker, datePickerEnd), 100);
    }

    private void initBundleHandler(Bundle savedInstanceState) {
        // read state of datepicker from prev changes
        if (savedInstanceState != null) {
            datePicker.setTimeInMillis(savedInstanceState.getLong("TIMESTAMP1", Calendar.getInstance().getTimeInMillis()));
            Calendar calEndDefault = Calendar.getInstance();
            calEndDefault.add(Calendar.MONTH, 1);
            datePickerEnd.setTimeInMillis(savedInstanceState.getLong("TIMESTAMP2", calEndDefault.getTimeInMillis()));
            userDomainId = savedInstanceState.getInt(STATE_USERDOMAIN_ID, 0);
        } else {
            int currentDate = datePicker.get(Calendar.DATE);
            int cycleDate = sharedpreferences.getInt("STATISTIK_DATE_CYCLE", 25);
            if (currentDate <= cycleDate) {
                datePicker.add(Calendar.MONTH, -1);
                datePicker.set(Calendar.DATE, cycleDate + 1);
                datePickerEnd.set(Calendar.DATE, cycleDate);
            } else {
                datePicker.set(Calendar.DATE, cycleDate + 1);
                datePickerEnd.add(Calendar.MONTH, 1);
                datePickerEnd.set(Calendar.DATE, cycleDate);
            }
        }
    }

    protected FragmentActivity getActivityNonNull() {
        FragmentActivity activity = this.getActivity();
        if (activity == null) {
            return null;
//            throw new IllegalStateException(
//                "Fragment " + this + " not attached to an activity."
//            );
        } else {
            return activity;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current datepicker
        outState.putLong("TIMESTAMP1", datePicker.getTimeInMillis());
        outState.putLong("TIMESTAMP2", datePickerEnd.getTimeInMillis());
        outState.putInt(STATE_USERDOMAIN_ID, userDomainId);
    }


    private void getKelasList(Calendar cal, Calendar cal2) {
        Log.i(TAG, "Fetching KelasList on '" + Utils.formatApiDate(cal.getTime()) + "' Started");

        // reset button next
        itemSelectionChanged(Boolean.FALSE);

        // reset if exist
        if (mCallKelas != null && mCallKelas.isExecuted())
            mCallKelas.cancel();

        mCallKelas = ServiceGenerator
                .createService(KelasService.class)
                .getClassActive(Utils.formatApiDate(cal.getTime()), Utils.formatApiDate(cal2.getTime()), userDomainId);

        // enqueue
        swipeRefreshKelas.setRefreshing(Boolean.TRUE);
        mCallKelas.enqueue(new Callback<List<Kelas>>() {
            @Override
            public void onResponse(Call<List<Kelas>> call, Response<List<Kelas>> response) {
                swipeRefreshKelas.setRefreshing(Boolean.FALSE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Kelas> newKelasList = response.body();
                    getKelasAndSectionList(newKelasList);

                    if (mKelasAndSectionList.isEmpty()) {
                        noResultInfo.setText(R.string.activeclass_noresult);
                        noResultInfo.setVisibility(View.VISIBLE);
                    }

                    // show notif list updated
                    if (!isFirstLoad) {
                        Toast.makeText(getActivityNonNull(), R.string.list_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        isFirstLoad = Boolean.FALSE;
                    }

                    Log.i(TAG, "Fetching KelasList Finished");
                } else {
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + response.raw());

                    getKelasAndSectionList(new ArrayList<>(0));
                    noResultInfo.setText(R.string.result_error);
                    noResultInfo.setVisibility(View.VISIBLE);
                    switch (response.code()) {
                        case 500:
                            Toast.makeText(getActivityNonNull(), "Terjadi kesalahan di server", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getActivityNonNull(), "Terjadi kesalahan yang tidak diketahui", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Kelas>> call, Throwable t) {
                swipeRefreshKelas.setRefreshing(Boolean.FALSE);
                t.printStackTrace();
                showNetworkErrorSnackbar();
            }
        });
    }

    /**
     *
     * @param newKelasList a new list of object Kelas
     */
    private void getKelasAndSectionList(List<Kelas> newKelasList) {
        mKelasAndSectionList.clear();
        String lastHeader = "";
        int size = newKelasList.size();

        for (int i = 0; i < size; i++) {
            Kelas k = newKelasList.get(i);
            String header = k.getPembinaan();
            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header;
                mKelasAndSectionList.add(new SectionGroupTitle(header));
            }

            mKelasAndSectionList.add(k);
        }
        sttViewAdapter.removeSelection();
    }

    /**
     * Show date range picker
     */
    private void setDateRangePicker() {
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                SttKelasFragment.this,
                datePicker.get(Calendar.YEAR),
                datePicker.get(Calendar.MONTH),
                datePicker.get(Calendar.DAY_OF_MONTH),
                datePickerEnd.get(Calendar.YEAR),
                datePickerEnd.get(Calendar.MONTH),
                datePickerEnd.get(Calendar.DAY_OF_MONTH)
        );

        dpd.setAccentColor(ContextCompat.getColor(getActivityNonNull(), R.color.colorPrimaryDarker));
        dpd.show(getActivityNonNull().getFragmentManager(), "Datepickerdialog");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        Log.d(TAG, String.format("Start: %d/%d/%d, End: %d/%d/%d", dayOfMonth, monthOfYear, year, dayOfMonthEnd, monthOfYearEnd, yearEnd));
        datePicker.set(year, monthOfYear, dayOfMonth);
        datePickerEnd.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);
        showLabelDatePicker(datePicker, datePickerEnd);
        getKelasList(datePicker, datePickerEnd);
    }

    /**
     * Show label calendar
     * @param calStart start calendar
     * @param calEnd end calendar
     */
    private void showLabelDatePicker(Calendar calStart, Calendar calEnd) {
        if (calStart.getTimeInMillis() == calEnd.getTimeInMillis()) {
            periode.setText(Utils.formatSimpleDate(calStart.getTime()));
        } else {
            periode.setText(String.format("%s - %s ", Utils.formatSimpleDate(calStart.getTime()), Utils.formatSimpleDate(calEnd.getTime())));
        }
    }

    @Override
    public void onRefresh() {
        getKelasList(this.datePicker, datePickerEnd);
    }

    @Subscribe
    public void onEventUserDomainChange(UserDomainChangedEvent event) {
        userDomainId = event.getIdentifier();
        getKelasList(this.datePicker, datePickerEnd);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_submit_pp:
                Intent i = new Intent(getActivityNonNull(), StatistikActivity.class);
                i.putExtra("KELASID", sttViewAdapter.getSelectedKelas().getId());
                i.putExtra("KELASNAME", sttViewAdapter.getSelectedKelas().getKelas());
                i.putExtra("TIMESTAMP1", Utils.formatApiDate(datePicker.getTime()));
                i.putExtra("TIMESTAMP2", Utils.formatApiDate(datePickerEnd.getTime()));
                i.putExtra("LABEL_TIMESTAMP", periode.getText().toString());
                i.putExtra("LABEL_TOTAL_KBM", sttViewAdapter.getSelectedKelas().getTotalKBM());

                startActivity(i);
                break;
            case R.id.et_periode:
                setDateRangePicker();
                break;
        }
    }

    @Override
    public void itemSelectionChanged(Boolean isAnyItemSelected) {
        float alpha = isAnyItemSelected ? 1f : 0.5f;
        btnNext.setEnabled(isAnyItemSelected);
        btnNext.setAlpha(alpha);
    }

    /**
     * Tampilkan snackbar network error
     */
    private void showNetworkErrorSnackbar() {
        try {
            View view = getActivityNonNull().findViewById(android.R.id.content);
            Utils.displayNetworkErrorSnackBar(view, null);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //cancel retrofit
        if (mCallKelas != null) mCallKelas.cancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // change toolbar title
        try {
            Toolbar toolbar = getActivityNonNull().findViewById(R.id.toolbar);
            toolbar.setTitle("Statistik Kelas");
        } catch (NullPointerException npe) {
            Log.e(TAG, npe.getMessage(), npe);
            npe.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister bus
        BusStation.getBus().unregister(this);
    }
}
