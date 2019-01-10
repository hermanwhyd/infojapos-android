package info.japos.pp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Calendar;

import info.japos.pp.R;
import info.japos.pp.constants.AppConstant;
import info.japos.pp.fragments.AboutDialog;
import info.japos.pp.fragments.JadwalPresensiFragment;
import info.japos.pp.fragments.SttPesertaFragment;
import info.japos.pp.models.listener.OnFragmentInteractionListener;
import info.japos.pp.fragments.StatistikFragment;
import info.japos.pp.helper.SessionManager;
import info.japos.pp.models.ApplicationInfo.ApplicationInfo;
import info.japos.pp.models.ApplicationInfo.VersionInfo;
import info.japos.pp.models.User;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.retrofit.LoginService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.VersionService;
import info.japos.pp.view.CustomToast;
import info.japos.utils.ApplicationUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {
    private static String TAG = MainActivity.class.getSimpleName();

    private User userLogged;
    private Calendar calendar = Calendar.getInstance();
    private Call<VersionInfo> callVersion;
    private Call<CommonResponse> callLogout;
    private SessionManager session;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Session manager
        session = new SessionManager(this.getApplication());
        // shared preferences
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // check session
        userLogged = session.getUserLoged();
        if (!session.isLoggedIn() || userLogged == null) {
            performLogout();
            return;
        }

        // Check Update
        long lastUpdateCheck = sharedpreferences.getLong(AppConstant.LAST_CHECK_UPDATE, 0L);
        long currTimeMillis = calendar.getTimeInMillis();
        long diff = currTimeMillis - lastUpdateCheck;
        Log.d(TAG, "Last check update: " + (diff/3600000) + "H");
        if (diff >= 7200000) { // 2h
            checkAppUpdate();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        // change textview on drawer
        TextView tvFullname = header.findViewById(R.id.tv_fullname);
        tvFullname.setText(userLogged.getNama());
        TextView tvEmail = header.findViewById(R.id.tv_email);
        tvEmail.setText(userLogged.getEmail());
        TextView tvVersion = navigationView.findViewById(R.id.footer_version);
        tvVersion.setText(getResources().getString(R.string.app_name));

        // check if only orientation changed
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_absensi);
            setSelectedFragment(R.id.nav_absensi);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setSelectedFragment(item.getItemId());
        return true;
    }

    public void setSelectedFragment(int id) {
        Fragment fragment = null;

        if (id == R.id.nav_absensi) {
            fragment = new JadwalPresensiFragment();
        } else if (id == R.id.nav_statistik) {
            fragment = new StatistikFragment();
        } else if (id == R.id.nav_statistik_siswa) {
            fragment = new SttPesertaFragment();
        } else if (id == R.id.nav_contactdev) {
            String url = getResources().getString(R.string.urlwa);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            performLogout();
        } else if (id == R.id.nav_about) {
            showAbout();
        }

        // Fragment changing code
        if(fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // Closing the drawer after selecting
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Check Application Update into Server
     */
    private void checkAppUpdate() {
        ApplicationInfo appInfo = ApplicationUtil.getApplicationVersionString(getBaseContext());
        callVersion = ServiceGenerator.createService(VersionService.class).getVersion(appInfo.getVersionCode());
        callVersion.enqueue(new Callback<VersionInfo>() {
            @Override
            public void onResponse(Call<VersionInfo> call, Response<VersionInfo> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    VersionInfo versionInfo = response.body();
                    Log.d(TAG, "Version loaded, response: " + versionInfo.toString());

                    // save into preff
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putLong(AppConstant.LAST_CHECK_UPDATE, calendar.getTimeInMillis());
                    editor.commit();

                    // compare
                    if ((appInfo.getVersionCode() < versionInfo.getVersionCode()
                            && versionInfo.getPrevVersionAction().equalsIgnoreCase("update"))
                            || appInfo.getVersionCode() < versionInfo.getMinVersionAllowed()) {
                        // must update
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Update Aplikasi")
                                .content("Ada versi aplikasi terbaru yang harus diinstall. Update sekarang?")
                                .positiveText("Ya")
                                .negativeText("Keluar")
                                .dismissListener(dialogInterface -> finish())
                                .onNegative((dialog, which) -> dialog.dismiss())
                                .onPositive((dialog, which) -> gotoDownloadPage(versionInfo.getDownloadUrl()))
                                .autoDismiss(Boolean.FALSE)
                                .show();
                    } else if (appInfo.getVersionCode() < versionInfo.getVersionCode()) {
                        // reminder only
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Update Aplikasi?")
                                .content("Ada versi aplikasi terbaru. Update sekarang?")
                                .positiveText("Ya")
                                .negativeText("Nanti saja")
                                .onNegative((dialog, which) -> dialog.dismiss())
                                .onPositive((dialog, which) -> gotoDownloadPage(versionInfo.getDownloadUrl()))
                                .show();
                    }
                } else {
                    CustomToast.show(getBaseContext(), "Gagal mendapatkan info Update");
                }
            }

            @Override
            public void onFailure(Call<VersionInfo> call, Throwable t) {
                Log.e(TAG, "Failed get info Update");
                t.printStackTrace();
            }
        });
    }

    /**
     * Show about info
     */
    private void showAbout() {
        AboutDialog.show(this);
    }

    /**
     * Open browser to download page
     * @param downloadUrl
     */
    private void gotoDownloadPage(String downloadUrl) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(downloadUrl));
        startActivity(i);
    }

    /**
     * Invalidate session, reset task (finish), and go to login page
     */
    public void performLogout() {
        callLogout = ServiceGenerator
                .createService(LoginService.class)
                .doLogout(session.getApiToken());
        callLogout.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {}

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {}
        });
        session.invalidate();
        Intent i = new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(i);
        this.finish();
    }

    @Override
    public void onFragmentInteraction(String title) {
        getSupportActionBar().setTitle(title);
    }

}
