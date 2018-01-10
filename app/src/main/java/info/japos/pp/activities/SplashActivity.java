package info.japos.pp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

import info.japos.pp.helper.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private static String TAG = SplashActivity.class.getSimpleName();
    private final int SPLASH_DISPLAY_LENGTH = 1500; // in milis
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // set locale set on device
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        SplashActivity.this.getResources().updateConfiguration(config, SplashActivity.this.getResources().getDisplayMetrics());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //request permissions. NOTE: Copying this and the manifest will cause the app to crash as the permissions requested aren't defined in the manifest.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
                    checkPermission();
                }

                //after two seconds, it will execute all of this code.
                if (session.isLoggedIn()) {
//                if (true) {
                    Intent goToMainActivity = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(goToMainActivity);
                } else {
                    Intent goToLoginActivity = new Intent(SplashActivity.this, LoginActivity.class);
                    SplashActivity.this.startActivity(goToLoginActivity);
                }

                //then we finish this class. Dispose of it as it is longer needed
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void checkPermission() {
        //Can add more as per requirement
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    123);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}
