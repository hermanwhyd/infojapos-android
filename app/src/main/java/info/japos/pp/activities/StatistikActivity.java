package info.japos.pp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import info.japos.pp.R;
import info.japos.pp.adapters.SttPesertaTableDataAdapter;
import info.japos.pp.models.statistik.StatistikGeneral;
import info.japos.pp.models.statistik.StatistikKelas;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.StatistikService;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.ProgresDialog;
import info.japos.pp.view.statistik.SortableStatistikPesertaTableView;
import info.japos.utils.ColorUtil;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatistikActivity extends AppCompatActivity {
    private static final String TAG = StatistikActivity.class.getSimpleName();

    private Menu menu;
    @BindView(R.id.tableViewStt)
    SortableStatistikPesertaTableView sttPesertaTableView;
    @BindView(R.id.tv_title_stt_peserta)
    TextView tvTitleSttPeserta;
    @BindView(R.id.tv_title_stt_kelas_pie)
    TextView tvTitleSttKelasPie;
    @BindView(R.id.chart_pie_kelas)
    PieChart chartPieKelas;
    @BindView(R.id.tv_title_stt_kelas_line)
    TextView tvTitleSttKelasLine;
    @BindView(R.id.chart_line_kelas)
    LineChart chartLineKelas;

    private SparseBooleanArray callCompleted = new SparseBooleanArray();;
    private Call<StatistikKelas> callSttKelas;
    private Call<StatistikKelas> callSttPeserta;
    private SttPesertaTableDataAdapter tableDataAdapter;
    private List<StatistikGeneral> sttPesertaList = new ArrayList<>(0);
    private MaterialDialog progresDialog;

    private int i_kelasid;
    private String i_timestamp1;
    private String i_timestamp2;
    private String i_label_timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistik);

        // binding
        ButterKnife.bind(this);

        initTableView();
        initIntentHandler();
        initProgressDialog();

        // set subtitle
        ActionBar actionBar = getSupportActionBar();
        SpannableString subtitle = new SpannableString(i_label_timestamp);
        subtitle.setSpan(new AbsoluteSizeSpan(30), 0, i_label_timestamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setSubtitle(subtitle);

        // post delay
        new Handler().postDelayed(() -> {
            populateSttPeserta(i_kelasid, i_timestamp1, i_timestamp2);
            populateSttKelas(i_kelasid, i_timestamp1, i_timestamp2);
        }, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.statistik_menu, menu);
        return true;
    }

    private void initProgressDialog() {
        progresDialog = ProgresDialog.showIndeterminateProgressDialog(this, R.string.progress_connecting_dialog, R.string.progress_please_wait, true);
        progresDialog.show();
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
        chartLineKelas.setScaleEnabled(false);

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
        dsHadir.setLineWidth(2f);
        LineDataSet dsAlpa = new LineDataSet(eAlpa, "Alpa");
        dsAlpa.setColor(ColorUtil.rgb("#FF3D00"));
        dsAlpa.setLineWidth(2f);
        LineDataSet dsIzin = new LineDataSet(eIzin, "Izin");
        dsIzin.setColor(ColorUtil.rgb("#FF9100"));
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
     * Init Table View Statistik
     */
    private void initTableView() {
        tableDataAdapter = new SttPesertaTableDataAdapter(this, sttPesertaList);
        sttPesertaTableView.setDataAdapter(tableDataAdapter);
        sttPesertaTableView.setSwipeToRefreshEnabled(Boolean.FALSE);
    }

    /**
     * mendapatkan data bundle yang dikirim dari main activity
     */
    private void initIntentHandler() {
        Intent i = getIntent();
        i_kelasid = i.getIntExtra("KELASID", 0);
        i_timestamp1 = i.getStringExtra("TIMESTAMP1");
        i_timestamp2 = i.getStringExtra("TIMESTAMP2");
        i_label_timestamp = i.getStringExtra("LABEL_TIMESTAMP");
    }

    /**
     * Fetch data into server, statistik kelas
     */
    private void populateSttKelas(int kelasId, String timestamp1, String timestamp2) {
        Log.i(TAG, "Fetching Statistik Kelas Started");
        int callKey = 101;
        callCompleted.put(callKey, Boolean.FALSE);
        callSttKelas = ServiceGenerator
                .createService(StatistikService.class)
                .getStatistikKelas(kelasId, timestamp1, timestamp2);

        callSttKelas.enqueue(new Callback<StatistikKelas>() {
            @Override
            public void onResponse(Call<StatistikKelas> call, Response<StatistikKelas> response) {
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
                    CustomToast.show(getApplication(), "Gagal mendapatkan data dari server");
                }
                notifyCallCompleted(callKey);
            }

            @Override
            public void onFailure(Call<StatistikKelas> call, Throwable t) {
                showNetworkErrorSnackbar();
                t.printStackTrace();
                notifyCallCompleted(callKey);
            }
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
        callCompleted.put(callKey, Boolean.FALSE);
        callSttPeserta = ServiceGenerator
                            .createService(StatistikService.class)
                            .getStatistikPeserta(kelasId, timestamp1, timestamp2);

        callSttPeserta.enqueue(new Callback<StatistikKelas>() {
            @Override
            public void onResponse(Call<StatistikKelas> call, Response<StatistikKelas> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    StatistikKelas statistikKelas = response.body();
                    menu.findItem(R.id.mn_statistik_kelas).setTitle(statistikKelas.getNamaKelas());
                    tvTitleSttPeserta.setText("Kehadiran per Siswa");
                    sttPesertaList.clear();
                    sttPesertaList.addAll(statistikKelas.getStatistikGenerals());
                    // notify to change data view
                    tableDataAdapter.notifyDataSetChanged();

                    if (refreshIndicator != null) {
                        refreshIndicator.hide();
                        Toast.makeText(StatistikActivity.this, R.string.list_updated, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    CustomToast.show(getApplication(), "Gagal mendapatkan data dari server");
                }

                notifyCallCompleted(callKey);
            }

            @Override
            public void onFailure(Call<StatistikKelas> call, Throwable t) {
                showNetworkErrorSnackbar();
                if (refreshIndicator != null) {
                    refreshIndicator.hide();
                }
                t.printStackTrace();
                notifyCallCompleted(callKey);
            }
        });
    }

    /**
     * Notify call completed
     */
    private synchronized void notifyCallCompleted(int key) {
        callCompleted.delete(key);
        if (callCompleted.size() == 0) progresDialog.dismiss();
    }

    /**
     * Tampilkan snackbar network error
     */
    private void showNetworkErrorSnackbar() {
        try {
            View view = findViewById(android.R.id.content);
            if (view != null) Utils.displayNetworkErrorSnackBar(view, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back Pressed");
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //cancel retrofit mCall saat activity didestroy
        if (callSttPeserta != null)
            callSttPeserta.cancel();

        if (callSttKelas != null)
            callSttKelas.cancel();
    }

}
