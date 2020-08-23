package org.logicprobe.printsizer;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.lifecycle.SavedStateHandle;

public final class Util {
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
}
