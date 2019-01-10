package info.japos.pp.view;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import info.japos.pp.R;

/**
 * Created by HWAHYUDI on 24-Dec-17.
 */

public class CustomSnackbar {
    /**
     * Funsi untuk menampilkan snackbar
     * @param text string pesan yang akan ditampilkan
     * @param actionName nama label button yang tampil. e.g. UNDO, RETRY, dll
     * @param action event action yang dilakukan
     */
    public static void displaySnackbar(Context context, View view, String text, String actionName, View.OnClickListener action) {
        Snackbar snack = Snackbar.make(view.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction(actionName, action);

        View v = snack.getView();
        v.setBackgroundColor(context.getResources().getColor(R.color.accent_translucent));
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) v.findViewById(android.support.design.R.id.snackbar_action)).setTextColor(Color.BLACK);

        snack.show();
    }
}
