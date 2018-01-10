package info.japos.pp.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by HWAHYUDI on 07-Jan-18.
 *
 * SAMPLE ONLY
 */

public class ShowcasePrefsManager {
    public static int SEQUENCE_NEVER_STARTED = 0;
    public static int SEQUENCE_FINISHED = -1;
    private static final String PREFS_NAME = "material_showcaseview_prefs";
    private static final String STATUS = "status_";
    private String showcaseID = null;
    private Context context;

    public ShowcasePrefsManager(Context context, String showcaseID) {
        this.context = context;
        this.showcaseID = showcaseID;
    }

    public boolean hasFired() {
        int status = this.getSequenceStatus();
        return status == SEQUENCE_FINISHED;
    }

    public void setFired() {
        this.setSequenceStatus(SEQUENCE_FINISHED);
    }

    int getSequenceStatus() {
        return this.context.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_" + this.showcaseID, SEQUENCE_NEVER_STARTED);
    }

    void setSequenceStatus(int status) {
        SharedPreferences internal = this.context.getSharedPreferences("material_showcaseview_prefs", 0);
        internal.edit().putInt("status_" + this.showcaseID, status).apply();
    }

    public void resetShowcase() {
        resetShowcase(this.context, this.showcaseID);
    }

    static void resetShowcase(Context context, String showcaseID) {
        SharedPreferences internal = context.getSharedPreferences("material_showcaseview_prefs", 0);
        internal.edit().putInt("status_" + showcaseID, SEQUENCE_NEVER_STARTED).apply();
    }

    public static void resetAll(Context context) {
        SharedPreferences internal = context.getSharedPreferences("material_showcaseview_prefs", 0);
        internal.edit().clear().apply();
    }

    public void close() {
        this.context = null;
    }

}
