package info.japos.pp;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import info.japos.pp.constants.AppConstant;
import info.japos.pp.models.realm.Enums;
import info.japos.pp.models.realm.EnumsRepository;
import info.japos.pp.retrofit.EnumsService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.CustomToast;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HWAHYUDI on 05-Dec-17.
 */

public class AppController extends Application {
    private static String TAG = AppController.class.getSimpleName();
    private SharedPreferences sharedpreferences;
    private static AppController controller;
    private Calendar calendar = Calendar.getInstance();

    public static synchronized AppController getInstance() {
        return controller;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        controller = this;

        // shared preferences
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // realms
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("info.japos.pp.realm")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        // jika lastFetch lebih dari config xx jam
        long lastFetch = sharedpreferences.getLong(AppConstant.LAST_GET_ENUM_IZIN, 0L);
        long currTimeMillis = calendar.getTimeInMillis();
        long diff = currTimeMillis - lastFetch;

        if (diff >= 7200000) fetchEnumsIzin(); // 2h
    }

    private void fetchEnumsIzin() {
        Call<List<Enums>> callEnums = ServiceGenerator.createService(EnumsService.class).getPilihanByGrup(AppConstant.ENUM_IZIN_ALASAN);
        callEnums.enqueue(new Callback<List<Enums>>() {
            @Override
            public void onResponse(Call<List<Enums>> call, Response<List<Enums>> response) {
                if (response.isSuccessful() && response.code() == 200) {
                    List<Enums> enums = response.body();
                    if (enums != null && !enums.isEmpty()) {
                        EnumsRepository.with(AppController.this).addEnums(enums);
                    }

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putLong(AppConstant.LAST_GET_ENUM_IZIN, calendar.getTimeInMillis());
                    editor.apply();
                } else {
                    CustomToast.show(getBaseContext(), "Gagal mendapatkan list 'Alasan Izin'");
                }
            }

            @Override
            public void onFailure(Call<List<Enums>> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
                t.printStackTrace();
            }
        });
    }
}
