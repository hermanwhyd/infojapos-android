package info.japos.pp.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment {
    private OnDateSetListener mDateSetListener;
    Calendar cal;

    public DatePickerDialogFragment() {
        cal = Calendar.getInstance();
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public DatePickerDialogFragment(OnDateSetListener callback, Calendar cal) {
        mDateSetListener = (OnDateSetListener) callback;
        this.cal = cal;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                mDateSetListener, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        // set saturday as first day
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            dialog.getDatePicker().setFirstDayOfWeek(Calendar.SATURDAY);

        return dialog;
    }
}
