package info.japos.pp.view;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import info.japos.pp.R;
import info.japos.utils.Utils;

/**
 * Created by HWAHYUDI on 04-Mar-18.
 */

public class MessageBoxDialog {
    public static void Show(Context context, String content) {
        new MaterialDialog.Builder(context)
                .content(content)
                .positiveText("OK")
                .autoDismiss(Boolean.TRUE)
                .show();
    }
}
