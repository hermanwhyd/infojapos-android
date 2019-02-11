package info.japos.pp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.activities.PresensiActivity;
import info.japos.pp.adapters.JadwalAdapter;
import info.japos.pp.helper.SessionManager;
import info.japos.pp.helper.ShowcasePrefsManager;
import info.japos.pp.models.PresensiInfoLog;
import info.japos.pp.models.kbm.common.ItemSectionInterface;
import info.japos.pp.models.kbm.common.SectionGroupTitle;
import info.japos.pp.models.kbm.jadwal.Jadwal;
import info.japos.pp.models.listener.OnFragmentInteractionListener;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.retrofit.JadwalService;
import info.japos.pp.retrofit.PresensiService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.EqualSpacingItemDecoration;
import info.japos.pp.view.MessageBoxDialog;
import info.japos.pp.view.ProgresDialog;
import info.japos.utils.ErrorUtils;
import info.japos.utils.GsonUtil;
import info.japos.utils.RecyclerColumnQty;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class JadwalPresensiFragment extends Fragment
        implements View.OnClickListener, JadwalAdapter.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener,
        DatePickerFragmentDialog.OnDateSetListener {

    private static final String TAG = JadwalPresensiFragment.class.getSimpleName();
    private OnFragmentInteractionListener mListener;
    private static final String STATE_DATE_PICKED = "datePicked";
    private Call<List<Jadwal>> mCallJadwal = null;
    private Call<CommonResponse> mCallPresensi = null;
    private Calendar datePicker = Calendar.getInstance();
    private JadwalAdapter jadwalAdapter;
    private ArrayList<ItemSectionInterface> mJadwalAndSectionList = new ArrayList<>();

    private boolean isFirstLoad = Boolean.TRUE;
    private SessionManager sessionManager;

    // Showcase config
    private static final String SHOWCASE_ID = "JadwalPresensiShowCase";

    public static final int REQUEST_CODE_PRESENSI = 100;
    public static final String EXTRA_KEY_ANY_CHANGES = "isAnyPresenceChanges";

    @BindView(R.id.et_tanggalkbm) EditText tanggalKMB;
    @BindView(R.id.rv_kelasjadwal) RecyclerView jadwalView;
    @BindView(R.id.tv_no_result) TextView noResultInfo;
    @BindView(R.id.btn_submit_pp) Button btnNext;
    @BindView(R.id.swipe_refresh_jadwal) SwipeRefreshLayout swipeRefreshJadwal;

    public JadwalPresensiFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mListener != null) {
            mListener.onFragmentInteraction("Jadwal KBM");
        }

        View view = inflater.inflate(R.layout.fragment_jadwal_presensi, container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(Boolean.FALSE);

        // shared preferences
        ShowcasePrefsManager showcasePrefsManager = new ShowcasePrefsManager(getActivity(), SHOWCASE_ID);

        // Session Manager
        sessionManager = new SessionManager(getActivity().getApplication());

        // butter knife binding
        ButterKnife.bind(this, view);

        // bind event onclick to this
        tanggalKMB.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        swipeRefreshJadwal.setOnRefreshListener(this);

        // swipe refresh
        swipeRefreshJadwal.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.BLUE);

        // RecyclerView
        RecyclerColumnQty recyclerColumnQty = new RecyclerColumnQty(jadwalView.getContext(), R.layout.item_jadwal);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(jadwalView.getContext(), recyclerColumnQty.calculateNoOfColumns());
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return JadwalAdapter.SECTION_VIEW == jadwalAdapter.getItemViewType(position) ? recyclerColumnQty.calculateNoOfColumns() : 1;
            }
        });

        jadwalView.addItemDecoration(new EqualSpacingItemDecoration(12, EqualSpacingItemDecoration.GRID)); // 8px. In practice, you'll want to use getDimensionPixelSize
        jadwalAdapter = new JadwalAdapter(mJadwalAndSectionList, getContext(), JadwalPresensiFragment.this);
        jadwalView.setLayoutManager(gridLayoutManager);
        jadwalView.setAdapter(jadwalAdapter);

        initBundleHandler(savedInstanceState);

        // set tanggalkbm text view
        tanggalKMB.setText(Utils.formatDate(datePicker.getTime()));
        tanggalKMB.setInputType(InputType.TYPE_NULL);

        // Showcase view
        if (!showcasePrefsManager.hasFired()) presentShowcaseSequence();

        // post runnable to run fetching data
        swipeRefreshJadwal.postDelayed(() -> getJadwalKBM(datePicker), 100);
    }

    private void initBundleHandler(Bundle savedInstanceState) {
        // read state of datepicker from prev changes
        if (savedInstanceState != null) {
            datePicker.setTimeInMillis(savedInstanceState.getLong(STATE_DATE_PICKED, Calendar.getInstance().getTimeInMillis()));
            Log.d(TAG, Utils.formatDate(datePicker.getTime()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current datepicker
        outState.putLong(STATE_DATE_PICKED, datePicker.getTimeInMillis());

        //cancel retrofit mCallJadwal kalau activity sudah didestroy
        if (mCallJadwal != null) mCallJadwal.cancel();
        if (mCallPresensi != null) mCallPresensi.cancel();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_submit_pp:
                Jadwal sJadwal = jadwalAdapter.getSelectedItem();

                if (TextUtils.isEmpty(sJadwal.getStatus()) || sJadwal.getStatus().equalsIgnoreCase("")) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Buat presensi baru?")
                            .content(R.string.presensi_createnew, sJadwal.getKelas(), Utils.formatDate(datePicker.getTime()), sJadwal.getJamMulai(), sJadwal.getJamSelesai())
                            .positiveText("Buat")
                            .negativeText("Batal")
                            .onPositive((dialog1, which) -> createNewPresensi(sJadwal))
                            .show();
                } else {
                    gotoPresenceActivity(sJadwal);
                }
                break;
            case R.id.et_tanggalkbm:
                DatePickerFragmentDialog dialog = DatePickerFragmentDialog.newInstance(this , datePicker.get(Calendar.YEAR), datePicker.get(Calendar.MONTH), datePicker.get(Calendar.DATE));
                dialog.setAccentColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDarker));
                dialog.setYearRange(2015, 2025);
                dialog.show(getFragmentManager(), "Date Picker");
                break;
        }
    }

    /**
     * Membuar presensi baru, setelah dibuat, open presensi aktiviti
     * @param sJadwal
     */
    private void createNewPresensi(Jadwal sJadwal) {
        MaterialDialog materialDialog = ProgresDialog.showIndeterminateProgressDialog(getActivity(), R.string.progress_connecting_dialog, R.string.progress_createnewprecense, true);
        materialDialog.show();
        mCallPresensi =  ServiceGenerator
                .createService(PresensiService.class)
                .createPresensi(sJadwal.getId(), Utils.formatApiDate(datePicker.getTime()), sessionManager.getApiToken());

        mCallPresensi.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                materialDialog.dismiss();
                if (response.isSuccessful()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            gotoPresenceActivity(sJadwal);
                        }
                    }, 300);
                } else {
                    CommonResponse commonResponse = ErrorUtils.parseError(response);
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + GsonUtil.getInstance().toJson(commonResponse));
                    switch (response.code()) {
                        case 401:
                        case 403:
                        case 409:
                            MessageBoxDialog.Show(getActivity(), commonResponse.getMessage());
                            break;
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
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                materialDialog.dismiss();
                t.printStackTrace();
                showNetworkErrorSnackbar();
            }
        });
    }

    /**
     * Perform open Presence Activity
     * @param sJadwal
     */
    private void gotoPresenceActivity(Jadwal sJadwal) {
        Intent i = new Intent(getActivity(), PresensiActivity.class);
        i.putExtra("JADWAL", GsonUtil.getInstance().toJson(sJadwal, Jadwal.class));
        i.putExtra("TIMESTAMP", Utils.formatApiDate(this.datePicker.getTime()));
        startActivityForResult(i, REQUEST_CODE_PRESENSI);
    }

    /**
     * Mengambil data jadwal kelas dari API
     */
    private void getJadwalKBM(Calendar cal) {
        Log.i(TAG, "Fetching KBM on '" + Utils.formatApiDate(cal.getTime()) + "' Started");

        // reset button next
        itemSelectionChanged(Boolean.FALSE);

        // reset if exist
        if (mCallJadwal != null && mCallJadwal.isExecuted())
            mCallJadwal.cancel();

        mCallJadwal = ServiceGenerator
                .createService(JadwalService.class)
                .getSchedule(Utils.formatApiDate(cal.getTime()));

        // enqueue
        swipeRefreshJadwal.setRefreshing(Boolean.TRUE);
        mCallJadwal.enqueue(new Callback<List<Jadwal>> () {
             @Override
             public void onResponse(Call<List<Jadwal>> call, Response<List<Jadwal>> response) {
                 swipeRefreshJadwal.setRefreshing(Boolean.FALSE);
                 if (response.isSuccessful()) {
                     List<Jadwal> jadwals = response.body();
                     getJadwalAndSectionList(jadwals);

                     if (jadwals.isEmpty()) {
                         noResultInfo.setText(R.string.kbm_noresult);
                         noResultInfo.setVisibility(View.VISIBLE);
                     } else {
                         noResultInfo.setVisibility(View.GONE);
                     }

                     // show notif list updated
                     if (!isFirstLoad) {
                         Toast.makeText(getActivity(), R.string.list_updated, Toast.LENGTH_SHORT).show();
                     } else {
                         isFirstLoad = Boolean.FALSE;
                     }

                     Log.i(TAG, "Fetching KBM Finished");
                 } else {
                     Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + response.raw());

                     getJadwalAndSectionList(new ArrayList<>(0));
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
             public void onFailure(Call<List<Jadwal>> call, Throwable t) {
                 swipeRefreshJadwal.setRefreshing(Boolean.FALSE);
                 Log.e(TAG, t.getMessage(), t);
                 noResultInfo.setText(R.string.result_error);
                 noResultInfo.setVisibility(View.VISIBLE);
                 showNetworkErrorSnackbar();
             }
        });
    }

    /**
     * Menampilkan data jadwal ke view
     * @param jadwals
     */
    private void getJadwalAndSectionList(List<Jadwal> jadwals) {
        mJadwalAndSectionList.clear();
        jadwalAdapter.removeSelection();
        String lastHeader = "";
        int size = jadwals.size();

        for (int i = 0; i < size; i++) {
            Jadwal j = jadwals.get(i);
            String header = j.getPembinaan();

            if (!TextUtils.equals(lastHeader, header)) {
                lastHeader = header;
                mJadwalAndSectionList.add(new SectionGroupTitle(header));
            }
            mJadwalAndSectionList.add(j);
        }

        jadwalAdapter.notifyDataSetChanged();
    }

    @Override
    public void itemSelectionChanged(Boolean isAnyItemSelected) {
        float alpha = isAnyItemSelected ? 1f : 0.5f;
        btnNext.setEnabled(isAnyItemSelected);
        btnNext.setAlpha(alpha);
    }

    @Override
    public void onMenuAction(Jadwal sJadwal, MenuItem menuItem) {
        // in case user trying to delete or get info uncreated presence
        if (sJadwal.getPresensiId() == null) {
            new MaterialDialog.Builder(getActivity())
                    .title("Presensi Info")
                    .content(R.string.presensi_infostatistik_empty, sJadwal.getKelas())
                    .positiveText("Ok")
                    .onPositive((dialog, which) -> dialog.dismiss())
                    .show();

            return;
        }

        switch (menuItem.getItemId()) {
            case R.id.mn_delete:
                Log.d(TAG, "Activity start deleting presence");
                new MaterialDialog.Builder(getActivity())
                        .title("Hapus presensi?")
                        .content(R.string.presensi_delete, sJadwal.getKelas(), Utils.formatDate(datePicker.getTime()), sJadwal.getJamMulai(), sJadwal.getJamSelesai())
                        .positiveText("Hapus")
                        .negativeText("Batal")
                        .onPositive((dialog, which) -> doDeletePresensi(sJadwal.getPresensiId()))
                        .show();
                break;
            case R.id.mn_info :
                Call<PresensiInfoLog> mCallPresensiLog = ServiceGenerator.createService(PresensiService.class).getPresensiStatistik(sJadwal.getPresensiId());

                mCallPresensiLog.enqueue(new Callback<PresensiInfoLog>() {
                    @Override
                    public void onResponse(Call<PresensiInfoLog> call, Response<PresensiInfoLog> response) {
                        if (response.isSuccessful() && response.code() == 200) {
                            PresensiInfoLog presensiInfoLog = response.body();
                            if (presensiInfoLog != null) {
                                String strDate = "";
                                try {
                                    Date date = Utils.parseFromMysql(presensiInfoLog.getCreatedDate());
                                    strDate = Utils.formatSimpleDate(date, "EEEE, dd/MMM/yyyy HH:mm");
                                } catch (ParseException e) {
                                    strDate = "-";
                                    e.printStackTrace();
                                }

                                new MaterialDialog.Builder(getActivity())
                                        .title("Presensi Info")
                                        .content(R.string.presensi_infostatistik, sJadwal.getKelas(), TextUtils.isEmpty(presensiInfoLog.getNamaLengkap()) ? presensiInfoLog.getCreatedBy() : presensiInfoLog.getNamaLengkap(),
                                                strDate, presensiInfoLog.getStatistik().getHadir(), presensiInfoLog.getStatistik().getAlpa(), presensiInfoLog.getStatistik().getIzin())
                                        .positiveText("Ok")
                                        .onPositive((dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PresensiInfoLog> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerFragmentDialog view, int year, int monthOfYear, int dayOfMonth) {
        this.datePicker.set(year, monthOfYear, dayOfMonth);
        tanggalKMB.setText(Utils.formatDate(this.datePicker.getTime()));

        // fetch data to server
        noResultInfo.setVisibility(View.GONE);
        isFirstLoad = Boolean.TRUE;
        getJadwalKBM(this.datePicker);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check if the requestCode is the wanted one and if the result is what we are expecting
        if (requestCode == REQUEST_CODE_PRESENSI && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Hore dapet response OK dari Presensi Activity!");
            getJadwalKBM(this.datePicker);
        }
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
        getJadwalKBM(this.datePicker);
    }

    /**
     * Perform showcase on first open
     */
    private void presentShowcaseSequence() {
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), SHOWCASE_ID);
        if (sequence.hasFired()) {
            Log.d(TAG, String.format("Showcase id '%s' sudah pernah ditampilkan", SHOWCASE_ID));
            return;
        }

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        sequence.setConfig(config);

        // add squence items
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget(tanggalKMB)
                        .setDismissText("Mengerti")
                        .setContentText("Klik disini untuk menampilkan jadwal KBM berdasarkan tanggal yang dipilih")
                        .withRectangleShape(true)
                        .setDismissStyle(Typeface.DEFAULT_BOLD)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget(noResultInfo)
                        .setDismissText("Mengerti")
                        .setContentText("Pilih salah satu dari jadwal yang tampil untuk memulai Presensi. Pesan khusus akan tampil jika tidak ada jadwal ditanggal yang dipilih.")
                        .setDismissStyle(Typeface.DEFAULT_BOLD)
                        .withRectangleShape(true)
                        .build()
        );
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(getActivity())
                        .setTarget(btnNext)
                        .setDismissText("Mulai")
                        .setContentText("Klik tombol Next untuk mulai Presensi, layar baru akan tampil dengan daftar nama-nama peserta KBM. Jika Presensi belum pernah dibuat sebelumnya, akan ada permintaan persetujuan membuat presensi baru terlebih dahulu")
                        .withRectangleShape(true)
                        .setDismissStyle(Typeface.DEFAULT_BOLD)
                        .build()
        );

        // start sequence
        sequence.start();
    }

    /**
     * Invoke server, Hapus presensi
     */
    private void doDeletePresensi(int presensiId) {
        Call<CommonResponse>  mCallPresensiDelete = ServiceGenerator.createService(PresensiService.class)
                .deletePresensi(presensiId, sessionManager.getApiToken());
        swipeRefreshJadwal.setRefreshing(Boolean.TRUE);

        mCallPresensiDelete.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Presensi berhasil dihapus", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    swipeRefreshJadwal.setRefreshing(Boolean.FALSE);
                    getJadwalKBM(datePicker);
                } else {
                    swipeRefreshJadwal.setRefreshing(Boolean.FALSE);
                    CommonResponse commonResponse = ErrorUtils.parseError(response);
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + GsonUtil.getInstance().toJson(commonResponse));
                    switch (response.code()) {
                        case 401:
                            MessageBoxDialog.Show(getActivity(), "Informasi", commonResponse.getMessage());
                            break;
                        case 403:
                            MessageBoxDialog.Show(getActivity(), "Informasi", commonResponse.getMessage());
                            break;
                        case 409:
                            MessageBoxDialog.Show(getActivity(), "Informasi", commonResponse.getMessage());
                            break;
                        case 500:
                            Toast.makeText(getActivity(), "Terjadi kesalahan di server", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            String message = (commonResponse != null && commonResponse.getMessage() != null && !commonResponse.getMessage().equalsIgnoreCase(""))
                                    ? commonResponse.getMessage() : "Terjadi kesalahan yang tidak diketahui";
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                swipeRefreshJadwal.setRefreshing(Boolean.FALSE);
                Log.e(TAG, t.getMessage(), t);
                showNetworkErrorSnackbar();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //cancel retrofit mCallJadwal kalau activity sudah didestroy
        if (mCallJadwal != null) mCallJadwal.cancel();
        if (mCallPresensi != null) mCallPresensi.cancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
