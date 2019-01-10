package info.japos.pp.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import info.japos.pp.R;
import info.japos.pp.adapters.SttPesertaTableDataAdapter;
import info.japos.pp.models.statistik.StatistikGeneral;
import info.japos.pp.models.statistik.StatistikKelas;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.StatistikService;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.statistik.SortableStatistikPesertaTableView;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableviewSttFragment extends Fragment {

    private static String TAG = TableviewSttFragment.class.getSimpleName();

    @BindView(R.id.tableViewStt)
    SortableStatistikPesertaTableView sttPesertaTableView;
    @BindView(R.id.tv_title_stt_peserta)
    TextView tvTitleSttPeserta;

    private int kelasId;
    private String timestamp1, timestamp2;
    private Call<StatistikKelas> callSttPeserta;
    private SttPesertaTableDataAdapter tableDataAdapter;
    private List<StatistikGeneral> sttPesertaList = new ArrayList<>(0);

    public TableviewSttFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tableview_stt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // butter knife binding
        ButterKnife.bind(this, view);

        // bundle Handler
        kelasId = getArguments().getInt("KELAS_ID");
        timestamp1 = getArguments().getString("TIMESTAMP1");
        timestamp2 = getArguments().getString("TIMESTAMP2");

        initTableView();

        new Handler().postDelayed(() -> {
            populateSttPeserta(kelasId, timestamp1, timestamp2);
        }, 100);
    }

    /**
     * Init Table View Statistik
     */
    private void initTableView() {
        tableDataAdapter = new SttPesertaTableDataAdapter(getActivity(), sttPesertaList);
        sttPesertaTableView.setDataAdapter(tableDataAdapter);
        sttPesertaTableView.setSwipeToRefreshEnabled(Boolean.FALSE);
        sttPesertaTableView.setSwipeToRefreshListener((refreshIndicator) -> {
            // your async refresh action goes here
            populateSttPeserta(kelasId, timestamp1, timestamp2, refreshIndicator);
        });
    }

    /**
     * Fetch data into server
     * @param kelasId
     * @param timestamp1
     * @param timestamp2
     */
    private void populateSttPeserta(int kelasId, String timestamp1, String timestamp2) {
        populateSttPeserta(kelasId, timestamp1, timestamp2, null);
    }

    /**
     * Fetch data into server
     * @param kelasId
     * @param timestamp1
     * @param timestamp2
     * @param refreshIndicator Refresh indicator from UI while user swipe down
     */
    private void populateSttPeserta(int kelasId, String timestamp1, String timestamp2, SwipeToRefreshListener.RefreshIndicator refreshIndicator) {
        Log.i(TAG, "Fetching Statistik Peserta Started");
        int callKey = 100;
        callSttPeserta = ServiceGenerator
                .createService(StatistikService.class)
                .getStatistikPeserta(kelasId, timestamp1, timestamp2);

        callSttPeserta.enqueue(new Callback<StatistikKelas>() {
            @Override
            public void onResponse(Call<StatistikKelas> call, Response<StatistikKelas> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    StatistikKelas statistikKelas = response.body();
                    tvTitleSttPeserta.setText("Kehadiran per Siswa");
                    sttPesertaList.clear();
                    sttPesertaList.addAll(statistikKelas.getStatistikGenerals());
                    // notify to change data view
                    tableDataAdapter.notifyDataSetChanged();

                    if (refreshIndicator != null) {
                        refreshIndicator.hide();
                        Toast.makeText(getActivity(), R.string.list_updated, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.show(getActivity(), "Gagal mendapatkan data dari server");
                }
            }

            @Override
            public void onFailure(Call<StatistikKelas> call, Throwable t) {
                showNetworkErrorSnackbar();
                if (refreshIndicator != null) {
                    refreshIndicator.hide();
                }
                t.printStackTrace();
            }
        });
    }

    /**
     * Tampilkan snackbar network error
     */
    private void showNetworkErrorSnackbar() {
        try {
            View view = getActivity().findViewById(android.R.id.content);
            if (view != null) Utils.displayNetworkErrorSnackBar(view, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    @Override
    public void onDestroy() {
        //cancel retrofit mCall saat activity didestroy
        if (callSttPeserta != null)
            callSttPeserta.cancel();

        super.onDestroy();
    }
}
