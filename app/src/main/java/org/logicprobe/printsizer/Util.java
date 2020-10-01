package org.logicprobe.printsizer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.lifecycle.SavedStateHandle;

import org.apache.commons.math3.fraction.FractionConversionException;
import org.logicprobe.printsizer.model.Fraction;

import java.util.Arrays;

public final class Util {
    public static final double EPSILON = 0.0001d;
    private static final Fraction ONE_SIXTH = new Fraction(1, 6);
    private static final Fraction ONE_TWELFTH = new Fraction(1, 12);
    private static final Fraction ONE_TWENTY_FOURTH = new Fraction(1, 24);
    private static final int[] FRACTION_DENOMINATORS = { 1, 2, 3, 4, 6, 12, 24 };
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

    public static Fraction safeGetStateFraction(SavedStateHandle state, String key, Fraction defaultValue) {
        Object obj = state.get(key);
        if (obj instanceof Fraction) {
            return (Fraction)obj;
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

    public static int closestStopsDenominator(int denominator) {
        int target = 0;
        for (int i = 0; i < FRACTION_DENOMINATORS.length; i++) {
            if (target == 0 || Math.abs(FRACTION_DENOMINATORS[i] - denominator) < Math.abs(FRACTION_DENOMINATORS[i] - target)) {
                target = FRACTION_DENOMINATORS[i];
            }
        }
        return target;
    }

    /**
     * Convert a floating point stops value into a fraction, constrained by the denominators
     * a user is capable of inputting via the user interface.
     *
     * @param stopsValue Value in floating point
     * @return Valid fraction, if possible. Null if invalid.
     */
    public static Fraction buildConstrainedStopsFraction(double stopsValue) {
        return buildConstrainedStopsFraction(stopsValue, FRACTION_DENOMINATORS);
    }

    /**
     * Convert a floating point stops value into a fraction, constrained by the denominators
     * provided.
     *
     * @param stopsValue Value in floating point
     * @param constraintList List of allowable denominators
     * @return Valid fraction, if possible. Null if invalid.
     */
    public static Fraction buildConstrainedStopsFraction(double stopsValue, int[] constraintList) {
        // Make sure the input value is a valid double
        if (Double.isNaN(stopsValue) || Double.isInfinite(stopsValue)) {
            return null;
        }

        // Make sure we actually have a constraint list
        if (constraintList == null || constraintList.length == 0) {
            return null;
        }

        // Don't try to process a zero fraction
        if (Math.abs(stopsValue) < EPSILON) {
            return Fraction.ZERO;
        }

        // Attempt a simple conversion, and return if the denominator is allowed
        try {
            Fraction fraction = new Fraction(stopsValue);
            if (fraction.getNumerator() > 0) {
                for (int i = constraintList.length - 1; i >= 0; --i) {
                    if (fraction.getDenominator() == constraintList[i]) {
                        return fraction;
                    }
                }
            }
        } catch (FractionConversionException ignored) {
            // If a simple conversion is not possible, then we shouldn't go any further
            return null;
        }

        // If the simple conversion didn't give us an allowed denominator, then iterate through
        // the list of allowed denominators and attempt to find the closest match.
        Fraction lastCandidate = null;
        double lastCandidateDouble = Double.NaN;
        for (int i = constraintList.length - 1; i >= 0; --i) {
            try {
                Fraction candidate = Fraction.getUnreducedFraction((int) Math.round(stopsValue * constraintList[i]), constraintList[i]);
                double candidateDouble = candidate.doubleValue();
                if (lastCandidate == null ||
                        Math.abs(stopsValue - candidateDouble) < Math.abs(stopsValue - lastCandidateDouble) ||
                        (Math.abs(candidateDouble - lastCandidateDouble) < EPSILON) && candidate.getDenominator() < lastCandidate.getDenominator()) {
                    lastCandidate = candidate;
                    lastCandidateDouble = candidateDouble;
                }
            } catch (FractionConversionException ignored) {
                // Ignore any candidates we cannot create
            }
        }

        // If the fraction represents zero or a whole number, then reduce it so that it
        // makes more sense.
        if (lastCandidate != null) {
            if (lastCandidate.getNumerator() == 0) {
                lastCandidate = Fraction.ZERO;
            } else if (Math.abs(lastCandidate.getNumerator()) == Math.abs(lastCandidate.getDenominator())) {
                lastCandidate = new Fraction(lastCandidate.getNumerator(), lastCandidate.getDenominator());
            }
        }

        return lastCandidate;
    }

    public static Fraction constrainedFractionAdd(Fraction first, Fraction second) {
        if (first == null || second == null) {
            return null;
        }
        // Start with a basic addition
        Fraction result = first.add(second);

        // If the first operand's denominator gives us an exact match, use that
        Fraction firstConverted = convertFractionToDenominator(result, first.getDenominator());
        if (Math.abs(firstConverted.doubleValue() - result.doubleValue()) < EPSILON) {
            return firstConverted;
        }

        // If the second operand's denominator gives us an exact match, use that
        Fraction secondConverted = convertFractionToDenominator(result, second.getDenominator());
        if (Math.abs(secondConverted.doubleValue() - result.doubleValue()) < EPSILON) {
            return secondConverted;
        }

        // If neither operand gave us an exact match, then pick the closest choice
        double resultValue = result.doubleValue();
        Fraction firstCandidate = Fraction.getUnreducedFraction((int) Math.round(resultValue * first.getDenominator()), first.getDenominator());
        Fraction secondCandidate = Fraction.getUnreducedFraction((int) Math.round(resultValue * second.getDenominator()), second.getDenominator());
        if (Math.abs(resultValue - firstCandidate.doubleValue()) < Math.abs(resultValue - secondCandidate.doubleValue())) {
            return firstCandidate;
        } else {
            return secondCandidate;
        }
    }

    /**
     * Convert a reduced fraction to an unreduced fraction with the specified denominator.
     * If the provided fraction cannot be directly converted, the result may only be
     * an estimate.
     *
     * @param fraction The fraction to convert
     * @param denominator The specified denominator to use
     * @return The modified fraction
     */
    public static Fraction convertFractionToDenominator(Fraction fraction, int denominator) {
        if (fraction == null) {
            return null;
        }

        denominator = Math.abs(denominator);

        if (fraction.getDenominator() == denominator) {
            return fraction;
        }

        int multiple = (int)Math.round((double)denominator / (double)fraction.getDenominator());
        return Fraction.getUnreducedFraction(fraction.getNumerator() * multiple, denominator);
    }
}
