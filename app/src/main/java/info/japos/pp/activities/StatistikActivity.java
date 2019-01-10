package info.japos.pp.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.adapters.TabsPagerAdapter;
import info.japos.pp.constants.NetworkConstant;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.StatistikService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatistikActivity extends AppCompatActivity {
    private static final String TAG = StatistikActivity.class.getSimpleName();

    @BindView(R.id.pagerStatistik)
    ViewPager pagerStatistik;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigationView;

    private TabsPagerAdapter mAdapter;

    private int i_kelasid;
    private String i_kelasname;
    private String i_timestamp1;
    private String i_timestamp2;
    private String i_label_timestamp;
    private int i_label_total_kbm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistik);

        // binding
        ButterKnife.bind(this);

        initIntentHandler();
        initViewPager();
        initButtonNavigationView();

        // set subtitle
        ActionBar actionBar = getSupportActionBar();
        SpannableString subtitle = new SpannableString(i_label_timestamp + " (" + i_label_total_kbm + " KBM)");
        subtitle.setSpan(new AbsoluteSizeSpan(30), 0, i_label_timestamp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        subtitle.setSpan(new StyleSpan(Typeface.BOLD), i_label_timestamp.length(), subtitle.length(), 0);
        subtitle.setSpan(new RelativeSizeSpan(0.5f), i_label_timestamp.length(), subtitle.length(), 0);
        actionBar.setSubtitle(subtitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.statistik_menu, menu);

        // change kelas name
        menu.findItem(R.id.mn_statistik_kelas).setTitle(i_kelasname);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_statistik_download:
                String downloadUrl = String.format("%sstatistik/kelas/%d/%s/%s/download", NetworkConstant.API_BASE_URL, i_kelasid, i_timestamp1, i_timestamp2);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(downloadUrl));
                startActivity(i);
//                downloadFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViewPager() {
        Bundle bundle = new Bundle();
        bundle.putInt("KELAS_ID", i_kelasid);
        bundle.putString("TIMESTAMP1", i_timestamp1);
        bundle.putString("TIMESTAMP2", i_timestamp2);

        // Init View Pager
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), bundle);
        pagerStatistik.setAdapter(mAdapter);

        pagerStatistik.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void initButtonNavigationView() {
        int[] images = {R.drawable.ic_chart_line, R.drawable.ic_table};
        int[] colors = {ContextCompat.getColor(this, R.color.colorPrimaryDark), ContextCompat.getColor(this, R.color.colorPrimaryDark)};

        bottomNavigationView.isWithText(Boolean.TRUE);
        bottomNavigationView.setTextActiveSize(getResources().getDimension(R.dimen.text_active));
        bottomNavigationView.setTextInactiveSize(getResources().getDimension(R.dimen.text_inactive));
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottomNavigationView.setUpWithViewPager(pagerStatistik, colors, images);
        bottomNavigationView.selectTab(0);
//        bottomNavigationView.willNotRecreate(Boolean.TRUE);
    }

    /**
     * mendapatkan data bundle yang dikirim dari main activity
     */
    private void initIntentHandler() {
        Intent i = getIntent();
        i_kelasid = i.getIntExtra("KELASID", 0);
        i_kelasname = i.getStringExtra("KELASNAME");
        i_timestamp1 = i.getStringExtra("TIMESTAMP1");
        i_timestamp2 = i.getStringExtra("TIMESTAMP2");
        i_label_timestamp = i.getStringExtra("LABEL_TIMESTAMP");
        i_label_total_kbm = i.getIntExtra("LABEL_TOTAL_KBM", 0);
    }

    private void downloadFile() {
        Call<ResponseBody> mCall = ServiceGenerator
                    .createService(StatistikService.class)
                    .downloadStatistik(i_kelasid, i_timestamp1, i_timestamp2);


        mCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "server contacted and has file");

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body());

                            Log.d(TAG, "file download was a success? " + writtenToDisk);
                            return null;
                        }
                    }.execute();
                } else {
                    Log.d(TAG, "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "error");
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "presensi_" + i_kelasname + "_" + i_timestamp1 + "-" + i_timestamp2 + ".xlsx");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
