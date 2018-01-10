package info.japos.pp.view;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by HWAHYUDI on 12/08/2017.
 */

public class CustomToast {
    private static Toast toast;
    public static void show(final Context context, final String message) {
        try {
            //make sure that the code will run on ui thread using handler post
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } catch (Exception e) {
            //in case context is not present, etc
        }
    }
}