package info.japos.pp.fragments;

import android.graphics.Color;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.models.statistik.StatistikGeneral;
import info.japos.pp.models.statistik.StatistikKelas;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.StatistikService;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.ProgresDialog;
import info.japos.utils.ColorUtil;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChartSttFragment extends Fragment {
    private static String TAG = ChartSttFragment.class.getSimpleName();

    @BindView(R.id.tv_title_stt_kelas_pie)
    TextView tvTitleSttKelasPie;
    @BindView(R.id.chart_pie_kelas)
    PieChart chartPieKelas;
    @BindView(R.id.tv_title_stt_kelas_line)
    TextView tvTitleSttKelasLine;
    @BindView(R.id.chart_line_kelas)
    LineChart chartLineKelas;
    private Call<StatistikKelas> callSttKelas;
    private MaterialDialog progressDialog;

    private int kelasId;
    private String timestamp1, timestamp2;

    public ChartSttFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_stt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // butter knife binding
        ButterKnife.bind(this, view);

        // bundle handler
        kelasId = getArguments().getInt("KELAS_ID");
        timestamp1 = getArguments().getString("TIMESTAMP1");
        timestamp2 = getArguments().getString("TIMESTAMP2");

        initProgressDialog();

        // post delay
        new Handler().postDelayed(() -> {
            populateSttKelas(kelasId, timestamp1, timestamp2);
        }, 100);
    }

    private void initProgressDialog() {
        progressDialog = ProgresDialog.showIndeterminateProgressDialog(getActivity(), R.string.progress_connecting_dialog, R.string.progress_please_wait, true);
    }

    /**
     * Fetch data into server, statistik kelas
     */
    private void populateSttKelas(int kelasId, String timestamp1, String timestamp2) {
        Log.i(TAG, "Fetching Statistik Kelas Started");
        progressDialog.show();
        callSttKelas = ServiceGenerator
            .createService(StatistikService.class)
            .getStatistikKelas(kelasId, timestamp1, timestamp2);

        callSttKelas.enqueue(new Callback<StatistikKelas>() {
            @Override
            public void onResponse(Call<StatistikKelas> call, Response<StatistikKelas> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.code() == 200) {
                    StatistikKelas statistikKelas = response.body();
                    tvTitleSttKelasPie.setText("Prosentase Keseluruhan");
                    tvTitleSttKelasLine.setText("Prosentase per Minggu");

                    // create chart if statistic result is not empty
                    if (!statistikKelas.getStatistikGenerals().isEmpty()) {
                        createPieChart(statistikKelas.getStatistikGenerals());
                        createLineChart(statistikKelas.getStatistikGenerals());
                    }
                } else {
                    CustomToast.show(getActivity(), "Gagal mendapatkan data dari server");
                }
            }

            @Override
            public void onFailure(Call<StatistikKelas> call, Throwable t) {
                progressDialog.dismiss();
                showNetworkErrorSnackbar();
                t.printStackTrace();
            }
        });
    }

    /**
     * Build statistik line per week
     */
    private void createLineChart(List<StatistikGeneral> statistikList) {
        chartLineKelas.setDrawGridBackground(false);
        chartLineKelas.getDescription().setEnabled(false);
        chartLineKelas.setDrawBorders(false);

        chartLineKelas.getAxisRight().setEnabled(true);
        chartLineKelas.getXAxis().setDrawAxisLine(false);
        chartLineKelas.getXAxis().setDrawGridLines(false);

        // enable touch gestures
        chartLineKelas.setTouchEnabled(true);

        // enable scaling and dragging
        chartLineKelas.setDragEnabled(true);
        chartLineKelas.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chartLineKelas.setPinchZoom(true);

        setLineChartDs(statistikList);
        chartLineKelas.animateY(1000);

        Legend l = chartLineKelas.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    private void setLineChartDs(List<StatistikGeneral> statistikList) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        ArrayList<Entry> eHadir = new ArrayList<>();
        ArrayList<Entry> eAlpa = new ArrayList<>();
        ArrayList<Entry> eIzin = new ArrayList<>();
        List<String> labels = new ArrayList<>(0);

        for (int i=0; i<statistikList.size(); i++) {
            StatistikGeneral s = statistikList.get(i);
            eHadir.add(new Entry(i, (float)s.getStatistik().getHadir()/s.getStatistik().getTotal() * 100));
            eAlpa.add(new Entry(i, (float)s.getStatistik().getAlpa()/s.getStatistik().getTotal() * 100));
            eIzin.add(new Entry(i, (float)s.getStatistik().getIzin()/s.getStatistik().getTotal() * 100));

            // add into label
            labels.add(i, s.getLabel());
        }

        LineDataSet dsHadir = new LineDataSet(eHadir, "Hadir");
        dsHadir.setColor(ColorUtil.rgb("#00E676"));
        dsHadir.setCircleColor(ColorUtil.rgb("#00E676"));
        dsHadir.setLineWidth(2f);
        LineDataSet dsAlpa = new LineDataSet(eAlpa, "Alpa");
        dsAlpa.setColor(ColorUtil.rgb("#FF3D00"));
        dsAlpa.setCircleColor(ColorUtil.rgb("#FF3D00"));
        dsAlpa.setLineWidth(2f);
        LineDataSet dsIzin = new LineDataSet(eIzin, "Izin");
        dsIzin.setColor(ColorUtil.rgb("#FF9100"));
        dsIzin.setCircleColor(ColorUtil.rgb("#FF9100"));
        dsIzin.setLineWidth(2f);

        dataSets.add(dsHadir);
        dataSets.add(dsAlpa);
        dataSets.add(dsIzin);

        LineData data = new LineData(dataSets);
        DecimalFormat mFormat = new DecimalFormat("###.#");
        data.setValueFormatter(((value, entry, dataSetIndex, viewPortHandler) -> mFormat.format(value) + "%"));

        // custom label XAxis
        XAxis xAxis = chartLineKelas.getXAxis();
        xAxis.setValueFormatter((value, axis) ->(value < 0 || value >= labels.size()) ? "" : labels.get((int)value));
        xAxis.setGranularityEnabled(Boolean.TRUE);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-15);
        xAxis.setAxisMinimum(0f);

        chartLineKelas.setData(data);
        chartLineKelas.invalidate();
    }

    /**
     * Build statistik prosentase kehadiran
     */
    private void createPieChart(List<StatistikGeneral> statistikList) {
        chartPieKelas.setBackgroundColor(Color.WHITE);
        chartPieKelas.getDescription().setEnabled(Boolean.FALSE);
        chartPieKelas.setUsePercentValues(Boolean.TRUE);

        chartPieKelas.setTransparentCircleColor(Color.WHITE);

        chartPieKelas.setRotationEnabled(false);
        chartPieKelas.setHighlightPerTapEnabled(true);

        setPieChartDs(statistikList);

        chartPieKelas.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        Legend l = chartPieKelas.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setXOffset(8f);

        // entry label styling
        chartPieKelas.setEntryLabelColor(Color.WHITE);
        chartPieKelas.setEntryLabelTextSize(12f);
    }

    private void setPieChartDs(List<StatistikGeneral> statistikList) {
        ArrayList<PieEntry> values = new ArrayList<>();
        int totalHadir = 0;
        int totalIzin = 0;
        int totalAlpa = 0;

        for (StatistikGeneral s : statistikList) {
            totalHadir += (s.getStatistik().getHadir() == null) ? 0 : s.getStatistik().getHadir();
            totalIzin += (s.getStatistik().getIzin() == null) ? 0 : s.getStatistik().getIzin();
            totalAlpa += (s.getStatistik().getAlpa() == null) ? 0 : s.getStatistik().getAlpa();
        }

        int totalKBM = totalHadir + totalAlpa + totalIzin;
        values.add(new PieEntry((float) totalHadir/totalKBM, "Hadir"));
        values.add(new PieEntry((float) totalAlpa/totalKBM, "Alpa"));
        values.add(new PieEntry((float) totalIzin/totalKBM, "Izin"));

        PieDataSet dataSet = new PieDataSet(values, "");
        dataSet.setSliceSpace(1f);
        dataSet.setSelectionShift(5f);

        dataSet.setColors(ColorUtil.rgb("#00E676"), ColorUtil.rgb("#FF3D00"), ColorUtil.rgb("#FF9100"));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);
        chartPieKelas.setData(data);

        chartPieKelas.invalidate();
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
        super.onDestroy();

        if (callSttKelas != null)
            callSttKelas.cancel();
    }
}
