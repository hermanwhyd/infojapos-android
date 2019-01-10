package info.japos.pp.view;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import info.japos.pp.R;

/**
 * Created by HWAHYUDI on 27-Dec-17.
 */

public class ProgresDialog {

    /**
     * Menampilkan progres dialog
     * @param horizontal, Boolean true mean horizontal else circle loop
     */
    public static MaterialDialog showIndeterminateProgressDialog(Context context, int title, int content, boolean horizontal) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .progressIndeterminateStyle(horizontal)
                .cancelable(false)
                .build();
    }
}
