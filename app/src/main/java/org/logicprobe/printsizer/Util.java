package org.logicprobe.printsizer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.lifecycle.SavedStateHandle;

import org.apache.commons.math3.fraction.Fraction;

public final class Util {
    public static final double EPSILON = 0.0001d;
    private static final Fraction ONE_SIXTH = new Fraction(1, 6);
    private static final Fraction ONE_TWELFTH = new Fraction(1, 12);
    private static final Fraction ONE_TWENTY_FOURTH = new Fraction(1, 24);

    private Util() { }

    /**
     * Helper method to hide the keyboard from an activity
     * https://medium.com/@rmirabelle/close-hide-the-soft-keyboard-in-android-db1da22b09d2
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Helper method to hide the keyboard from a fragment
     * https://medium.com/@rmirabelle/close-hide-the-soft-keyboard-in-android-db1da22b09d2
     */
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean hasText(EditText editText) {
        return editText != null
                && editText.getText() != null
                && editText.getText().length() > 0
                && editText.getText().toString().trim().length() > 0;
    }

    public static String safeGetEditTextString(EditText editText) {
        if (editText.getText() != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }

    public static boolean safeGetStateBoolean(SavedStateHandle state, String key) {
        Object obj = state.get(key);
        if (obj instanceof Boolean) {
            return (Boolean)obj;
        } else {
            return false;
        }
    }

    public static int safeGetStateInt(SavedStateHandle state, String key, int defaultValue) {
        Object obj = state.get(key);
        if (obj instanceof Integer) {
            return (Integer)obj;
        } else {
            return defaultValue;
        }
    }

    public static long safeGetStateLong(SavedStateHandle state, String key, long defaultValue) {
        Object obj = state.get(key);
        if (obj instanceof Long) {
            return (Long)obj;
        } else {
            return defaultValue;
        }
    }

    public static double safeGetStateDouble(SavedStateHandle state, String key, double defaultValue) {
        Object obj = state.get(key);
        if (obj instanceof Double) {
            return (Double)obj;
        } else {
            return defaultValue;
        }
    }

    public static boolean isValidNonZero(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && Math.abs(value) > EPSILON;
    }

    public static boolean isValidPositive(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value > EPSILON;
    }

    public static boolean isNightMode(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        int currentNightMode = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                // Assume false if unknown
                return false;
        }
    }

    public static Fraction preferenceValueToFraction(String prefValue) {
        if ("1_stop".equals(prefValue)) {
            return Fraction.ONE;
        } else if ("1_2_stop".equals(prefValue)) {
            return Fraction.ONE_HALF;
        } else if ("1_3_stop".equals(prefValue)) {
            return Fraction.ONE_THIRD;
        } else if ("1_4_stop".equals(prefValue)) {
            return Fraction.ONE_QUARTER;
        } else if ("1_6_stop".equals(prefValue)) {
            return ONE_SIXTH;
        } else if ("1_12_stop".equals(prefValue)) {
            return ONE_TWELFTH;
        } else if ("1_24_stop".equals(prefValue)) {
            return ONE_TWENTY_FOURTH;
        } else {
            return Fraction.ZERO;
        }
    }
}
