package info.japos.pp.fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.ganfra.materialspinner.MaterialSpinner;
import info.japos.pp.R;
import info.japos.pp.adapters.SttPesertaViewAdapter;
import info.japos.pp.bus.BusStation;
import info.japos.pp.bus.events.UserDomainChangedEvent;
import info.japos.pp.models.ClassParticipant;
import info.japos.pp.models.Custom.StringWithTag;
import info.japos.pp.models.kbm.kelas.Kelas;
import info.japos.pp.models.listener.OnItemSelected;
import info.japos.pp.retrofit.KelasService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.EqualSpacingItemDecoration;
import info.japos.pp.view.MessageBoxDialog;
import info.japos.utils.RecyclerColumnQty;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SttPesertaFragment extends Fragment implements View.OnClickListener, OnItemSelected, SwipeRefreshLayout.OnRefreshListener {
    private static String TAG = SttPesertaFragment.class.getSimpleName();

    private List<StringWithTag> kelasList = new LinkedList<>();
    private Call<List<Kelas>> mCallKelas;
    private Call<List<ClassParticipant>> mCallPeserta;
    private Boolean isFirstLoad = Boolean.TRUE;

    private int userDomainId;

    private List<ClassParticipant> pesertaList = new ArrayList<>(0);
    private SttPesertaViewAdapter pesertaAdapter;
    private Integer kelasId;

    @BindView(R.id.sp_kelaskbm) MaterialSpinner kelasSpinner;
    @BindView(R.id.rv_peserta) RecyclerView kelasView;
    @BindView(R.id.tv_no_result) TextView noResultInfo;
    @BindView(R.id.btn_submit_pp) Button btnNext;
    @BindView(R.id.swipe_refresh_peserta) SwipeRefreshLayout swipeRefreshPeserta;

    public SttPesertaFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stt_peserta, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get bundle params
        Bundle args = getArguments();
        userDomainId = args.getInt("UserDomainId");
    }

    @Override
    public void onResume() {
        super.onResume();

        // register bus
        BusStation.getBus().register(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(Boolean.FALSE);

        // butter knife binding
        ButterKnife.bind(this, view);

        // init spinner
        initSpinnerKelas();

        // swipe refresh
        swipeRefreshPeserta.setEnabled(Boolean.FALSE);
        swipeRefreshPeserta.setOnRefreshListener(this);
        swipeRefreshPeserta.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.BLUE);

        // init button
        btnNext.setOnClickListener(this);

        // init recycler
        RecyclerColumnQty recyclerColumnQty = new RecyclerColumnQty(getActivity(), R.layout.item_peserta);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),recyclerColumnQty.calculateNoOfColumns());
        kelasView.addItemDecoration(new EqualSpacingItemDecoration(12, EqualSpacingItemDecoration.VERTICAL)); // 8px. In practice, you'll want to use getDimensionPixelSize
        pesertaAdapter = new SttPesertaViewAdapter(pesertaList, getContext(), this);
        kelasView.setAdapter(pesertaAdapter);
        kelasView.setLayoutManager(gridLayoutManager);

        // init
        getKelasList();
    }

    protected FragmentActivity getActivityNonNull() {
        if (super.getActivity() != null) {
            return super.getActivity();
        } else {
            throw new RuntimeException("null returned from getActivityNonNull()");
        }
    }

    private void initSpinnerKelas() {
        ArrayAdapter<StringWithTag> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, kelasList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelasSpinner.setAdapter(adapter);

        kelasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > -1) {
                    StringWithTag item = (StringWithTag) adapterView.getItemAtPosition(position);
                    kelasId = (Integer) item.tag;
                    populateParticipants(kelasId);
                } else { // Pilih Kelas
                    kelasView.setVisibility(View.GONE);
                    noResultInfo.setVisibility(View.VISIBLE);
                    noResultInfo.setText(R.string.kelas_no_selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    /**
     * Mengambil data siswa ke server
     *
     */
    private void populateParticipants(final int kelasId) {
        Log.i(TAG, "Fetching class participants Started");

        mCallPeserta = ServiceGenerator
                .createService(KelasService.class)
                .getClassParticipant(kelasId);

        // enqueue
        swipeRefreshPeserta.setRefreshing(Boolean.TRUE);
        mCallPeserta.enqueue(new Callback<List<ClassParticipant>>() {
            @Override
            public void onResponse(Call<List<ClassParticipant>> call, Response<List<ClassParticipant>> response) {
                swipeRefreshPeserta.setRefreshing(Boolean.FALSE);
                if (response.isSuccessful() && response.code() == 200) {
                    Log.i(TAG, "Fetching class participants Finished");
                    if (response.body() != null) {
                        List<ClassParticipant> tmpPartcps = response.body();
                        pesertaList.clear();
                        pesertaList.addAll(tmpPartcps);
                        pesertaAdapter.removeSelection();

                        kelasView.setVisibility(View.VISIBLE);
                        noResultInfo.setText(R.string.peserta_noresult);
                        noResultInfo.setVisibility(pesertaList.size() == 0 ? View.VISIBLE : View.GONE);

                        // show notif list updated
                        if (!isFirstLoad) {
                            Toast.makeText(getActivity(), R.string.list_updated, Toast.LENGTH_SHORT).show();
                        } else {
                            isFirstLoad = Boolean.FALSE;
                        }
                    } else {
                        noResultInfo.setText(R.string.result_error);
                        noResultInfo.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message());
                    CustomToast.show(getActivity(), "Gagal mendapatkan data dari server");
                }
            }

            @Override
            public void onFailure(Call<List<ClassParticipant>> call, Throwable t) {
                noResultInfo.setText(R.string.presensi_error);
                noResultInfo.setVisibility(View.VISIBLE);
                swipeRefreshPeserta.setRefreshing(Boolean.FALSE);
                showNetworkErrorSnackbar();
                t.printStackTrace();
            }
        });
    }

    /**
     * Mengambil data jadwal kelas dari API
     */
    private void getKelasList() {
        Calendar cal = Calendar.getInstance();
        Log.i(TAG, "Fetching KelasList on '" + Utils.formatApiDate(cal.getTime()) + "' Started");

        // reset if exist
        if (mCallKelas != null && mCallKelas.isExecuted())
            mCallKelas.cancel();

        mCallKelas = ServiceGenerator
                .createService(KelasService.class)
                .getClassActive(Utils.formatApiDate(cal.getTime()), userDomainId);

        // enqueue
        mCallKelas.enqueue(new Callback<List<Kelas>>() {
            @Override
            public void onResponse(Call<List<Kelas>> call, Response<List<Kelas>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Kelas> newKelasList = response.body();
                    kelasList.clear();
                    for (Kelas kelas : newKelasList) {
                        kelasList.add(new StringWithTag(kelas.getKelas(), kelas.getId()));
                    }

                    if (kelasList.isEmpty()) {
                        noResultInfo.setText(R.string.activeclass_noresult);
                        noResultInfo.setVisibility(View.VISIBLE);
                    }

                    swipeRefreshPeserta.setEnabled(Boolean.TRUE);
                    Log.i(TAG, "Fetching KelasList Finished");
                } else {
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + response.raw());

                    noResultInfo.setText(R.string.result_error);
                    noResultInfo.setVisibility(View.VISIBLE);
                    switch (response.code()) {
                        case 500:
                            Toast.makeText(getActivity(), "Terjadi kesalahan di server", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getActivity(), "Terjadi kesalahan yang tidak diketahui", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Kelas>> call, Throwable t) {
                t.printStackTrace();
                showNetworkErrorSnackbar();
            }
        });
    }

    /**
     * Tampilkan snackbar network error
     */
    private void showNetworkErrorSnackbar() {
        try {
            View view = getActivity().findViewById(android.R.id.content);
            Utils.displayNetworkErrorSnackBar(view, null);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        populateParticipants(kelasId);
    }

    @Subscribe
    public void onEventUserDomainChange(UserDomainChangedEvent event) {
        Log.d(TAG, "Receive: UserDomain changed event");
        userDomainId = event.getIdentifier();
        getKelasList();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_submit_pp:
                MessageBoxDialog.Show(getContext(), "Next Feature", "Fitur ini sedang dalam pengembangan, nantikan diupdetan selanjutnya.");

        }
    }

    @Override
    public void itemSelectionChanged(Boolean isAnyItemSelected) {
        float alpha = isAnyItemSelected ? 1f : 0.5f;
        btnNext.setEnabled(isAnyItemSelected);
        btnNext.setAlpha(alpha);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // change toolbar title
        Toolbar toolbar = getActivityNonNull().findViewById(R.id.toolbar);
        toolbar.setTitle("Jadwal KBM");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //cancel retrofit mCall saat activity didestroy
        if (mCallPeserta != null) {
            mCallPeserta.cancel();
        }
        if (mCallKelas != null) {
            mCallKelas.cancel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister bus
        BusStation.getBus().unregister(this);
    }
}
