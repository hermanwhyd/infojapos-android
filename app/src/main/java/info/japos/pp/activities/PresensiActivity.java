package info.japos.pp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.adapters.PresensiViewAdapter;
import info.japos.pp.constants.AppConstant;
import info.japos.pp.fragments.JadwalPresensiFragment;
import info.japos.pp.helper.SessionManager;
import info.japos.pp.helper.ShowcasePrefsManager;
import info.japos.pp.helper.ToolbarPresensiActionModeCallback;
import info.japos.pp.models.Jadwal;
import info.japos.pp.models.Peserta;
import info.japos.pp.models.Presensi;
import info.japos.pp.models.enums.PresensiKet;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.realm.Enums;
import info.japos.pp.models.realm.EnumsRepository;
import info.japos.pp.retrofit.JadwalService;
import info.japos.pp.retrofit.PresensiService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.EqualSpacingItemDecoration;
import info.japos.utils.BabushkaText;
import info.japos.utils.ErrorUtils;
import info.japos.utils.GsonUtil;
import info.japos.utils.Utils;
import info.japos.vendor.SwipeToAction;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class PresensiActivity extends AppCompatActivity implements PresensiViewAdapter.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = PresensiActivity.class.getSimpleName();

    @BindView(R.id.rv_presensi)
    RecyclerView presensiView;
    @BindView(R.id.swipe_refresh_presensi)
    SwipeRefreshLayout swipeRefreshPresensi;
    @BindView(R.id.presensi_header)
    BabushkaText presensiHeader;
    @BindView(R.id.tv_no_peserta)
    TextView tvNoResult;
    @BindView(R.id.tv_prolog_presensi) TextView tvPrologPresensi;
    private LinearLayoutManager linearLayoutManager;
    private PresensiViewAdapter presensiAdapter;
    private Presensi presensi;
    private List<Peserta> pesertaList = new ArrayList<>(0);
    private Jadwal iJadwal;
    private String iTimestamp;
    private List<String> izinReasons = new ArrayList<>(0);
    private SwipeToAction swipeToAction;
    private Vibrator mVibrator;
    private boolean isFirstLoad = Boolean.TRUE;
    private int idxListSorting;

    private SharedPreferences sharedpreferences;

    // Showcase config
    private static final String SHOWCASE_ID = "PresensiShowCase";
    private ShowcasePrefsManager showcasePrefsManager;

    private SessionManager sessionManager;
    private ActionMode mActionMode;

    Call<Presensi> mCall = null;
    Call<CommonResponse> mCallUpdPresensi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presensi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // binding
        ButterKnife.bind(this);
        swipeRefreshPresensi.setOnRefreshListener(this);

        // shared preferences
        showcasePrefsManager = new ShowcasePrefsManager(this, SHOWCASE_ID);
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // init index sorting
        idxListSorting = sharedpreferences.getInt("prefSortingIdx", 0);

        // Swipe
        swipeRefreshPresensi.setColorSchemeColors(Color.CYAN, Color.YELLOW, Color.BLUE);

        // Session
        sessionManager = new SessionManager(this.getApplication());

        // init recycler
        linearLayoutManager = new LinearLayoutManager(this);
        presensiView.setLayoutManager(linearLayoutManager);
        presensiView.addItemDecoration(new EqualSpacingItemDecoration(12, EqualSpacingItemDecoration.VERTICAL)); // 8px. In practice, you'll want to use getDimensionPixelSize
        presensiView.setHasFixedSize(Boolean.TRUE);
        presensiAdapter = new PresensiViewAdapter(pesertaList, this, PresensiActivity.this);
        presensiView.setAdapter(presensiAdapter);

        initSwipeToAction();
        initIntentHandler();

        // iterate enums izin alasan
        RealmResults<Enums> enums = EnumsRepository.with(this).getEnumsByGrup(AppConstant.ENUM_IZIN_ALASAN);
        for (Enums anEnum : enums) {
            izinReasons.add(anEnum.getValue());
        }
        Log.d(TAG, "Enums from db: " + GsonUtil.getInstance().toJson(izinReasons));

        // Create Vibrator instance for current context
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Showcase view
        if (!showcasePrefsManager.hasFired()) presentShowcaseSequence();

        // post runnable to run fetching data
        swipeRefreshPresensi.post(new Runnable() {
            @Override
            public void run() {
                populateStudents(iJadwal, iTimestamp);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.presensi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.mn_presensi_sortby:
                onSortByPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Inisialisasi swipe to action
     */
    private void initSwipeToAction() {
        swipeToAction = new SwipeToAction(presensiView, new SwipeToAction.SwipeListener<Peserta>() {
            @Override
            public boolean swipeLeft(Peserta itemData) {
                updateKetPresensi(presensi, itemData, PresensiKet.H);
                return true;
            }

            @Override
            public boolean swipeRight(Peserta itemData) {
                updateKetAlasanPresensi(presensi, itemData);
                return true;
            }

            @Override
            public void onBlur(SwipeToAction.ViewHolder viewHolder, Peserta itemData) {
                Log.d(TAG, "Item Recycler Blur");
                if (swipeToAction.isInMultipleSelectionMode())
                    onListItemSelected(viewHolder, itemData);
//                presensiAdapter.togglePressedState((PresensiViewAdapter.StatistikViewHolder) viewHolder, Boolean.FALSE);
            }

            @Override
            public void onPress(SwipeToAction.ViewHolder viewHolder, Peserta itemData) {
                Log.d(TAG, "Item Recycler Press");
//                onListItemSelected(viewHolder, itemData);
            }

            @Override
            public void onLongPress(SwipeToAction.ViewHolder viewHolder, Peserta itemData) {
                Log.d(TAG, "Item Recycler LONG Press");
                mVibrator.vibrate(30);

                onListItemSelected(viewHolder, itemData);
            }
        });
    }

    // List item select method
    private void onListItemSelected(SwipeToAction.ViewHolder viewHolder, Peserta peserta) {
        presensiAdapter.toggleSelectionState((PresensiViewAdapter.PresensiViewHolder) viewHolder, peserta);

        // check if no selected, mark swipemode not in mutipleselection
        boolean hasSelectedItem = presensiAdapter.getSelectedCount() > 0;
        if (hasSelectedItem && mActionMode == null) {
            mActionMode = this.startSupportActionMode(new ToolbarPresensiActionModeCallback(this));
        } else if (!hasSelectedItem && mActionMode != null) {
            Log.d(TAG, "No item selected, then set inMultipleSelectionMode disabled!");
            swipeToAction.setInMultipleSelectionMode(Boolean.FALSE);
            finishActionMode();
        }

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf(presensiAdapter.getSelectedCount()));
    }

    /**
     * mendapatkan data bundle yang dikirim dari prev activity
     */
    private void initIntentHandler() {
        Intent i = getIntent();
        iJadwal = GsonUtil.getInstance().fromJson(i.getStringExtra("JADWAL"), Jadwal.class);
        iTimestamp = i.getStringExtra("TIMESTAMP");

        Date date = Utils.parseApiDate(iTimestamp);
        presensiHeader.addPiece(new BabushkaText.Piece(new BabushkaText.Piece.Builder("Kelas " + iJadwal.getKelas()).textSize(45).style(Typeface.BOLD).textColor(Utils.getColor(this, R.color.text_color))));
        presensiHeader.addPiece(new BabushkaText.Piece(new BabushkaText.Piece.Builder("\n" + Utils.formatDate(date)).textSizeRelative(0.9f).textColor(Utils.getColor(this, R.color.text_sub_gray))));
        presensiHeader.display();
    }

    /**
     * /**
     * Update keterangan izin presensi
     *
     * @param presensi
     * @param peserta
     */
    private void updateKetAlasanPresensi(Presensi presensi, Peserta peserta) {
        // radiobutton alasan yang sudah tercentang
        if (this.izinReasons.isEmpty()) {
            Resources res = getResources();
            String[] reason = res.getStringArray(R.array.izin_alasan);
            for (String alasan : reason) {
                this.izinReasons.add(alasan);
            }
        }

        Integer idxChecked = this.izinReasons.isEmpty()
                ? -1 : (peserta.getAlasan() == null
                ? 0 : izinReasons.indexOf(peserta.getAlasan()) > -1
                ? izinReasons.indexOf(peserta.getAlasan()) : 0);

        Log.d(TAG, "Index Reason: " + idxChecked);

        new MaterialDialog.Builder(PresensiActivity.this)
                .title("Alasan Izin")
                .items(this.izinReasons)
                .itemsCallbackSingleChoice(
                        idxChecked,
                        (dialog, view, which, text) -> {
                            peserta.setAlasan(text.toString());
                            Log.d(TAG, "Alasan izin: " + text);

                            updateKetPresensi(presensi, peserta, PresensiKet.I);
                            return true; // allow selection
                        })
                .onNegative((dialog, which) -> CustomToast.show(this, "Izin berhasil dibatalkan"))
                .onPositive((dialog, which) -> dialog.dismiss())
                .positiveText("Pilih")
                .negativeText("Batal")
                .autoDismiss(true)
                .show();
    }

    /**
     * Update keterangan (HASI) presensi
     *
     * @param presensi
     * @param peserta
     * @param ket
     */
    private void updateKetPresensi(Presensi presensi, Peserta peserta, final PresensiKet ket) {
        // check if same keterangan
        if (peserta.getKeterangan().equalsIgnoreCase(ket.name()) && !peserta.getKeterangan().equalsIgnoreCase(ket.I.name())) {
            CustomToast.show(getApplication(), peserta.getNamaPanggilan() + " telah diset " + ket.getValue() + " sebelumnya!");
            return;
        }

        updateKetPresensi(presensi, Arrays.asList(peserta), ket);
    }

    private void updateAllSelectedItem(final PresensiKet ket) {
        Log.d(TAG, "Selected : " + GsonUtil.getInstance().toJson(presensiAdapter.getSelectedPeserta()));
        updateKetPresensi(presensi, presensiAdapter.getSelectedPeserta(), ket);
    }

    /**
     * Update keterangan (HASI) presensi
     *
     * @param presensi
     * @param pesertaList
     * @param ket
     */
    private void updateKetPresensi(Presensi presensi, List<Peserta> pesertaList, final PresensiKet ket) {
        // update presensi
        for (Peserta peserta : pesertaList) {
            peserta.setKeterangan(ket.name());
        }

        presensi.getListPeserta().clear();
        presensi.getListPeserta().addAll(pesertaList);

        Log.i(TAG, "Update ket presensi: PresensiId->" + presensi.getId() + ", Object->" + GsonUtil.getInstance().toJson(presensi));

        swipeRefreshPresensi.setRefreshing(Boolean.TRUE);
        mCallUpdPresensi = ServiceGenerator
                .createService(PresensiService.class)
                .updatePresensi(presensi.getId(), presensi, sessionManager.getApiToken());

        mCallUpdPresensi.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                swipeRefreshPresensi.setRefreshing(Boolean.FALSE);
                if (response.isSuccessful() && response.code() == 200) {
                    showSuccessUpdateSnackbar(pesertaList, ket);
                    presensiAdapter.notifyDataSetChanged();
                } else {
                    CommonResponse commonResponse = ErrorUtils.parseError(response);
                    Log.e(TAG, "Caught error code: " + response.code() + ", message: " + response.message() + ". Details: " + GsonUtil.getInstance().toJson(commonResponse));
                    switch (response.code()) {
                        case 401:
                            Toast.makeText(PresensiActivity.this, commonResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                        case 403:
                            Toast.makeText(PresensiActivity.this, commonResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(PresensiActivity.this, "Terjadi kesalahan di server", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            String message = (commonResponse != null && commonResponse.getMessage() != null && !commonResponse.getMessage().equalsIgnoreCase(""))
                                    ? commonResponse.getMessage() : "Terjadi kesalahan yang tidak diketahui";
                            Toast.makeText(PresensiActivity.this, message, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                swipeRefreshPresensi.setRefreshing(Boolean.FALSE);
                showNetworkErrorSnackbar();
                t.printStackTrace();
            }
        });
    }

    /**
     * Mengambil data siswa ke server dengan jadwal id 'jadwalID' dan tanggal Presensi 'datePresence'
     *
     * @param jadwal    Object Jadwal
     * @param timestamp String date dengan format dd-MM-yyyy, adalah tanggal presensi
     */
    private void populateStudents(final Jadwal jadwal, String timestamp) {
        Log.i(TAG, "Fetching Students class presence Started");
        swipeRefreshPresensi.setRefreshing(Boolean.TRUE);

        // clear all selection and close ActionMode
        finishActionMode();
        setNullToActionMode();

        mCall = ServiceGenerator
                .createService(JadwalService.class)
                .getStudentPresences(jadwal.getId(), timestamp);

        mCall.enqueue(new Callback<Presensi>() {
            @Override
            public void onResponse(Call<Presensi> call, Response<Presensi> response) {
                swipeRefreshPresensi.setRefreshing(Boolean.FALSE);
                if (response.isSuccessful() && response.code() == 200) {
                    presensi = response.body();
                    Log.i(TAG, "Fetching Students class presence Finished");
                    if (presensi != null) {
                        Log.i(TAG, "Response: " + GsonUtil.getInstance().toJson(presensi, Presensi.class));
                        pesertaList.clear();
                        pesertaList.addAll(presensi.getListPeserta());
                        sortPesertaList();
                        tvNoResult.setText(R.string.presensi_noresult);
                        tvNoResult.setVisibility(pesertaList.size() == 0 ? View.VISIBLE : View.GONE);

                        // show notif list updated
                        if (!isFirstLoad) {
                            Toast.makeText(PresensiActivity.this, R.string.list_updated, Toast.LENGTH_SHORT).show();
                        } else {
                            isFirstLoad = Boolean.FALSE;
                        }
                    } else {
                        tvNoResult.setVisibility(View.VISIBLE);
                    }
                } else {
                    CustomToast.show(getApplication(), "Gagal mendapatkan data dari server");
                }
            }

            @Override
            public void onFailure(Call<Presensi> call, Throwable t) {
                tvNoResult.setText(R.string.presensi_error);
                tvNoResult.setVisibility(View.VISIBLE);
                swipeRefreshPresensi.setRefreshing(Boolean.FALSE);
                showNetworkErrorSnackbar();
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onRefresh() {
        populateStudents(iJadwal, iTimestamp);
    }

    @Override
    public void onPresensiSelected(Peserta peserta) {
        Log.i(TAG, "Item selected: " + GsonUtil.getInstance().toJson(peserta));
    }

    @Override
    public void onPresensiMenuAction(Peserta peserta, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_hadir:
                updateKetPresensi(presensi, peserta, PresensiKet.H);
                break;
            case R.id.mn_izin:
                updateKetAlasanPresensi(presensi, peserta);
                break;
            case R.id.mn_alpa:
                updateKetPresensi(presensi, peserta, PresensiKet.A);
                break;
        }
    }

    // close Action Mode
    public void finishActionMode() {
        if (mActionMode != null) mActionMode.finish();
    }

    //Set action mode null after use
    public void setNullToActionMode() {
        Log.d(TAG, "ActionMode destroyed!");
        if (mActionMode != null) {
            mActionMode = null;
        }
        presensiAdapter.removeSelection(); // remove selection
        swipeToAction.setInMultipleSelectionMode(Boolean.FALSE);
    }

    /**
     * Tampilkan snackbar network error
     */
    private void showNetworkErrorSnackbar() {
        View view = findViewById(android.R.id.content);
        if (view != null) Utils.displayNetworkErrorSnackBar(view, null);
    }

    private void showSuccessUpdateSnackbar(List<Peserta> pesertaList, PresensiKet ket) {
        View view = findViewById(android.R.id.content);
        if (view != null) {
            Peserta firstPeserta = pesertaList.get(0);
            Snackbar snackbar;
            if (pesertaList.size() > 1) {
                snackbar = Snackbar.make(view, getResources().getString(R.string.presensi_batch_success, firstPeserta.getNamaPanggilan(), (pesertaList.size() - 1), ket.getValue()), Snackbar.LENGTH_LONG);
            } else {
                snackbar = Snackbar.make(view, getResources().getString(R.string.presensi_success, firstPeserta.getNamaPanggilan(), ket.getValue()), Snackbar.LENGTH_LONG);
            }
            snackbar.getView().setBackgroundColor(Utils.getColor(view.getContext(), R.color.success_snackbar));
            snackbar.show();
        }
    }

    /**
     * Tandai semua item yang dipilih sesuai @param 'ket'
     *
     * @param ket
     */
    public void markAllSelected(PresensiKet ket) {
        new MaterialDialog.Builder(this)
                .title("Tandai semua peserta?")
                .content(String.format("Tandai semua peserta yang dipilih sebagai %s?", ket.getValue()))
                .positiveText("OK")
                .negativeText("Batal")
                .onPositive((dialog1, which) -> updateAllSelectedItem(ket))
                .show();
    }

    /**
     * Function called when menu sortby pressed
     */
    private void onSortByPressed() {
        Resources res = getResources();
        List<String> sortByList = Arrays.asList(res.getStringArray(R.array.presensi_sortby));

        new MaterialDialog.Builder(PresensiActivity.this)
                .title("Urutkan List Peserta")
                .items(sortByList)
                .itemsCallbackSingleChoice(
                        idxListSorting,
                        (dialog, view, which, text) -> {
                            idxListSorting = which;
                            sortPesertaList();

                            // update as shared prefference
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putInt("prefSortingIdx", which);
                            editor.commit();

                            return true; // allow selection
                        })
                .onNegative((dialog, which) -> dialog.dismiss())
                .onPositive((dialog, which) -> dialog.dismiss())
                .positiveText("Urutkan")
                .negativeText("Batal")
                .autoDismiss(true)
                .show();
    }

    /**
     * Sort peserta by selected menu sort order
     * idxList is index of resource array 'presensi_sortby'
     */
    private void sortPesertaList() {
        Log.d(TAG, "Sort pesertaList, idxListSorting: " + idxListSorting);
        Peserta[] pesertaArr = pesertaList.toArray(new Peserta[pesertaList.size()]);
        if (idxListSorting == 0)
            Arrays.sort(pesertaArr, Peserta::compareTo);
        else if (idxListSorting == 1)
            Arrays.sort(pesertaArr, Peserta.NicknameComparator);
        else if (idxListSorting == 2)
            Arrays.sort(pesertaArr, Peserta.KelompokComparator);
        else if (idxListSorting == 3)
            Arrays.sort(pesertaArr, Peserta.GenderComparator);
        else
            Arrays.sort(pesertaArr, Peserta::compareTo);

        pesertaList.clear();
        pesertaList.addAll(Arrays.asList(pesertaArr));
        presensiAdapter.notifyDataSetChanged();

        if (!isFirstLoad) {
            Toast.makeText(PresensiActivity.this, R.string.list_updated, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // jika tidak null, berarti pernah digunakan
        if (mCallUpdPresensi != null) {
            Intent i = new Intent();
            i.putExtra(JadwalPresensiFragment.EXTRA_KEY_ANY_CHANGES, Boolean.TRUE);
            setResult(Activity.RESULT_OK, i);
        }
        Log.d(TAG, "Back Pressed");
        finish();
    }

    /**
     * Perform showcase on first open
     */
    private void presentShowcaseSequence() {
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
        if (sequence.hasFired()) {
            Log.d(TAG, String.format("Showcase id '%s' sudah pernah ditampilkan", SHOWCASE_ID));
            return;
        }

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        sequence.setConfig(config);

        // add squence items
        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(tvPrologPresensi)
                        .setDismissText("Mulai")
                        .setDismissOnTouch(Boolean.TRUE)
                        .setContentText("Geser kanan atau kiri di item peserta untuk memberikan status kehadiran (Hadir/Alpa/Izin). Bisa juga multi pilihan dengan tekang dan tahan lalu pilih peserta lain dengan tekan pada item peserta, setelah selesai untuk set status kehadiran terdapat menu pilihan di pojok kanan atas aplikasi. Khusus untuk status kehadiran IZIN, akan ada popup alasan izin")
                        .withRectangleShape(true)
                        .setDismissStyle(Typeface.DEFAULT_BOLD)
                        .build()
        );

        sequence.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //cancel retrofit mCall saat activity didestroy
        if (mCall != null) {
            mCall.cancel();
        }
        if (mCallUpdPresensi != null) {
            mCallUpdPresensi.cancel();
        }
    }
}
