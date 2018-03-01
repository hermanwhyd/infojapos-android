package info.japos.pp.fragments;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import info.japos.pp.R;
import info.japos.pp.models.ApplicationInfo.ApplicationInfo;
import info.japos.utils.ApplicationUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ApplicationInfo applicationInfo = ApplicationUtil.getApplicationVersionString(getContext());
        return new MaterialDialog.Builder(getActivity())
                .title("About")
                .positiveText("OK")
                .content(getString(R.string.about_body, applicationInfo.getVersionName(), applicationInfo.getVersionCode()))
                .contentLineSpacing(1.2f)
                .build();
    }

    public static void show(AppCompatActivity context) {
        AboutDialog dialog = new AboutDialog();
        dialog.show(context.getSupportFragmentManager(), "[ABOUT_DIALOG]");
    }

}
