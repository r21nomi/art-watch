package com.nomi.artwatch.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
public class WindowUtil {

    public static int getWidth(final Context context) {
        final Display display = getDisplay(context);
        final Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getHeight(final Context context) {
        final Display display = getDisplay(context);
        final Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static Point getSize(final Context context) {
        final Display display = getDisplay(context);
        final Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (getDensity(context) * (float) dp + 0.5f);
    }

    public static float getDensity(Context context) {
        DisplayMetrics metrics = getDisplayMetrics(context);
        return metrics.density;
    }

    private static Display getDisplay(final Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static DisplayMetrics getDisplayMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
}
