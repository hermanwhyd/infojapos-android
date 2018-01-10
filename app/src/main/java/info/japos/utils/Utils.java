package info.japos.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import info.japos.pp.R;
import info.japos.pp.view.CustomToast;


/**
 * Created by HWAHYUDI on 12/08/2017.
 */

public class Utils {
    /**
     * Workaround to get color from various versions of Android
     *
     * @param context context
     * @param id      color resource id
     * @return int color
     */
    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static void openUrl(String url, Context ctx) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browserIntent);
        } catch (Exception e) {
            CustomToast.show(ctx, "Can't open url!");
        }
    }

    /**
     * Convert dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dp2px(float dp, Context context) {
        //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public static Spanned getHtmlSpan(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(message);
        }
    }

    /**
     *
     * @param date Date
     * @return String of param date with format dd/mm/yyyy
     */
    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        return dateFormat.format(date);
    }

    /**
     *
     * @param date Date
     * @return String of param date with format dd-MM-yyyy
     */
    public static String formatApiDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(date);
    }

    /**
     *
     * @param strDate String of param date with format dd/MM/yyyy
     * @return Utils.Date
     */
    public static Date parseApiDate(String strDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = dateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Tampilkan snackbar error
     * @param view
     * @param listener an event callback listener
     */
    public static void displayNetworkErrorSnackBar(View view, View.OnClickListener listener) {
        if (view != null && listener != null) {
            Snackbar snackbar = Snackbar.make(view, view.getContext().getString(R.string.network_error), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("RETRY", listener);
            snackbar.setActionTextColor(Color.RED);
            snackbar.getView().setBackgroundColor(Utils.getColor(view.getContext(), R.color.error_snackbar));
            snackbar.show();

        } else if (view != null) {
            Snackbar snackbar = Snackbar.make(view, view.getContext().getString(R.string.network_error), Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(Utils.getColor(view.getContext(), R.color.error_snackbar));
            snackbar.show();
        }
    }

    public static boolean isConnectedToInternet() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            // Check if exit value == 0, exit status = 0 => success status...
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
