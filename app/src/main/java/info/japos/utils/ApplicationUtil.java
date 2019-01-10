package info.japos.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import info.japos.pp.models.ApplicationInfo.ApplicationInfo;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class ApplicationUtil {
    /**
     * Get Application version
     * @param context
     * @return
     */
    public static ApplicationInfo getApplicationVersionString(Context context) {
        try {
            ApplicationInfo app = new ApplicationInfo();
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);

            app.setVersionName("v" + info.versionName);
            app.setVersionCode(info.versionCode);

            return app;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
