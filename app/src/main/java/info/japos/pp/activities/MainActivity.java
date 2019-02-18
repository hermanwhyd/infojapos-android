package info.japos.pp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableBadgeDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import info.japos.pp.R;
import info.japos.pp.bus.BusStation;
import info.japos.pp.bus.events.FragmentResumedEvent;
import info.japos.pp.bus.events.UserDomainChangedEvent;
import info.japos.pp.constants.AppConstant;
import info.japos.pp.fragments.AboutDialog;
import info.japos.pp.fragments.JadwalPresensiFragment;
import info.japos.pp.fragments.SttKelasFragment;
import info.japos.pp.helper.SessionManager;
import info.japos.pp.models.ApplicationInfo.ApplicationInfo;
import info.japos.pp.models.ApplicationInfo.VersionInfo;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.realm.User;
import info.japos.pp.models.realm.UserDomain;
import info.japos.pp.models.realm.UserDomainRepository;
import info.japos.pp.models.realm.UserRepository;
import info.japos.pp.retrofit.LoginService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.retrofit.VersionService;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.MessageBoxDialog;
import info.japos.utils.ApplicationUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity{
    private static String TAG = MainActivity.class.getSimpleName();

    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer result = null;
    private User userLogged;

    private Calendar calendar = Calendar.getInstance();
    private Call<VersionInfo> callVersion;
    private Call<CommonResponse> callLogout;
    private SessionManager session;
    private SharedPreferences sharedpreferences;

    @BindView (R.id.domain_info)
    TextView tvDomainInfo;

    // TextDrawable
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL; // or use DEFAULT
    private TextDrawable.IBuilder tBuilder = TextDrawable.builder()
            .beginConfig()
                .width(60)
                .height(60)
            .fontSize(20)
            .bold()
            .toUpperCase()
            .endConfig()
            .rect();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        BusStation.getBus().register(this);
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

        // binding
        ButterKnife.bind(this);

        // Check Update
        long lastUpdateCheck = sharedpreferences.getLong(AppConstant.LAST_CHECK_UPDATE, 0L);
        long currTimeMillis = calendar.getTimeInMillis();
        long diff = currTimeMillis - lastUpdateCheck;
        if (diff >= 7200000) { // 2h
            checkAppUpdate();
        }

        handleNavDrawable(savedInstanceState, userLogged);
    }

    private void handleNavDrawable(Bundle savedInstanceState, User userLogged) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        List<IProfile> profiles = new ArrayList<>();

        // get from db
        List<UserDomain> userDomainList = UserDomainRepository.with(this).getAllUserDomain();
        for (UserDomain dom : userDomainList) {
            profiles.add(new ProfileDrawerItem()
                    .withName(dom.getPembina())
                    .withEmail(dom.getNama())
                    .withIcon(tBuilder.build(dom.getInisial(), mColorGenerator.getColor(dom.getNama())))
                    .withIdentifier(dom.getId())
            );
        }

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header_ppg)
                .withProfiles(profiles)
                .withOnAccountHeaderListener((view, profile, current) -> {
                    // Toast notif domain changed
                    if (profile instanceof IDrawerItem && !current) {
                        tvDomainInfo.setText(String.format("%s %s", profile.getName(), profile.getEmail().getText()));

                        // save selected profile into realm db
                        UserDomain profileDomain = UserDomainRepository.getInstance().getUserDomain((int)profile.getIdentifier());
                        User userLoggedUpd = new User(userLogged.getId(), userLogged.getUsername(), userLogged.getEmail(), userLogged.getNama(), userLogged.getPassword(), profileDomain);
                        (UserRepository.with(this)).AddUser(userLoggedUpd);

                        // send event
                        BusStation.getBus().post(new UserDomainChangedEvent(profileDomain.getId()));
                    }

                    //false if you have not consumed the event and it should close the drawer
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Presensi").withIcon(FontAwesome.Icon.faw_leanpub).withIdentifier(11),
                        new ExpandableBadgeDrawerItem().withName("Statistik Kehadiran").withIcon(FontAwesome.Icon.faw_chart_pie).withIdentifier(20).withSelectable(false).withSubItems(
                            new SecondaryDrawerItem().withName("Kelas").withLevel(2).withIcon(FontAwesome.Icon.faw_chart_bar1).withIdentifier(21),
                            new SecondaryDrawerItem().withName("Peserta").withLevel(2).withIcon(FontAwesome.Icon.faw_user1).withIdentifier(22).withSelectable(false)
                        ),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Logout").withIcon(FontAwesome.Icon.faw_sign_out_alt).withIdentifier(31).withSelectable(false),
                        new SecondaryDrawerItem().withName("Contact Admin").withIcon(FontAwesome.Icon.faw_whatsapp).withIdentifier(32).withSelectable(false),
                        new SecondaryDrawerItem().withName("About").withIcon(FontAwesome.Icon.faw_question_circle).withIdentifier(33).withSelectable(false)
                )
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(userLogged.getNama()).withIcon(FontAwesome.Icon.faw_user_circle1).withIdentifier(91).withSelectable(false)
                )// add the items we want to use with our Drawer
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem != null) {
                        Fragment fragment = null;

                        if (drawerItem.getIdentifier() == 11) {
                            fragment = new JadwalPresensiFragment();
                        } else if (drawerItem.getIdentifier() == 21) {
                            fragment = new SttKelasFragment();
                        } else if (drawerItem.getIdentifier() == 22) {
                            MessageBoxDialog.Show(this, "Info", "Halaman ini sedang dalam pengembangan.");
//                            fragment = new SttPesertaFragment();
                        } else if (drawerItem.getIdentifier() == 31) {
                            performLogout();
                        } else if (drawerItem.getIdentifier() == 32) {
                            String url = getResources().getString(R.string.urlwa);
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        } else if (drawerItem.getIdentifier() == 33) {
                            showAbout();
                        } else if (drawerItem.getIdentifier() == 91) {
                            MessageBoxDialog.Show(this, "Info", "Halaman ini sedang dalam pengembangan.");
                        }

                        // Fragment changing code
                        if(fragment != null) {
                            Bundle arguments = new Bundle();
                            arguments.putInt("UserDomainId", userLogged.getActiveUserDomain().getId());
                            fragment.setArguments(arguments);

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_frame, fragment);
                            ft.commit();
                        }
                    }

                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
//              .withShowDrawerUntilDraggedOpened(true)
                .build();

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            result.setSelection(11, true);

            // set active profile domain
            setActiveProfileDomain();
        }
    }

    private void setActiveProfileDomain() {
        //set the active profile
        tvDomainInfo.setText(String.format("%s %s", userLogged.getActiveUserDomain().getPembina(), userLogged.getActiveUserDomain().getNama()));
        headerResult.setActiveProfile(userLogged.getActiveUserDomain().getId());
    }

    @Subscribe
    public void onFragmentResume(FragmentResumedEvent fm) {
        Log.i(TAG, "EventReceived: fragment resumed");
        setActiveProfileDomain();
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
                        // save into preff
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putLong(AppConstant.LAST_CHECK_UPDATE, calendar.getTimeInMillis());
                        editor.apply();

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
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else if (result != null && result.getCurrentSelection() != 11) {
            result.setSelection(11, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //cancel retrofit
        if (callVersion != null) callVersion.cancel();
        if (callLogout != null) callLogout.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Main Pause");
        BusStation.getBus().unregister(this);
    }
}
