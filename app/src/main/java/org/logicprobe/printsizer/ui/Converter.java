package org.logicprobe.printsizer.ui;

import androidx.annotation.StringRes;

import org.apache.commons.math3.util.FastMath;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.model.Fraction;
import org.logicprobe.printsizer.model.PaperProfile;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Converter {
    public static String focalLengthToString(double focalLength) {
        if (focalLength > 0) {
            NumberFormat f = NumberFormat.getInstance();
            f.setMinimumFractionDigits(0);
            f.setGroupingUsed(false);
            if (f instanceof DecimalFormat) {
                ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
            }
            return f.format(focalLength);
        } else {
            return "";
        }
    }

    public static String calculatedTimeToString(double time) {
        if (!Double.isNaN(time) && !Double.isInfinite(time) && time > 0.0d) {
            DecimalFormat format = new DecimalFormat("##.0");
            return format.format(time);
        } else {
            return "";
        }
    }

    @StringRes
    public static int paperGradeToResourceId(@PaperProfile.GradeId int gradeId) {
        switch (gradeId) {
            case PaperProfile.GRADE_00:
                return R.string.label_grade_00;
            case PaperProfile.GRADE_0:
                return R.string.label_grade_0;
            case PaperProfile.GRADE_1:
                return R.string.label_grade_1;
            case PaperProfile.GRADE_2:
                return R.string.label_grade_2;
            case PaperProfile.GRADE_3:
                return R.string.label_grade_3;
            case PaperProfile.GRADE_4:
                return R.string.label_grade_4;
            case PaperProfile.GRADE_5:
                return R.string.label_grade_5;
            case PaperProfile.GRADE_NONE:
                return R.string.label_grade_unfiltered;
            default:
                return R.string.empty;
        }
    }

    public static String secondsValueToString(double seconds, int maxFractionDigits) {
        if (Util.isValidNonZero(seconds)) {
            NumberFormat f = NumberFormat.getInstance();
            f.setMinimumFractionDigits(0);
            f.setMaximumFractionDigits(maxFractionDigits);
            f.setGroupingUsed(false);
            if (f instanceof DecimalFormat) {
                ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
            }
            return f.format(seconds);
        } else {
            return "";
        }
    }

    public static String secondsValueToString(double seconds) {
        return secondsValueToString(seconds, 2);
    }

    public static String burnDodgeSecondsDisplayToString(double seconds) {
        StringBuilder buf = new StringBuilder();
        if (!Util.isValidNonZero(seconds) || Math.abs(seconds) < 0.1d) {
            buf.append('0');
        } else if (seconds < 0d) {
            buf.append(secondsValueToString(seconds, 1));
        } else {
            buf.append('+');
            buf.append(secondsValueToString(seconds, 1));
        }
        return buf.toString();
    }

    public static String stopsValueToString(Fraction stopsValue) {
        if (stopsValue == null) {
            stopsValue = Fraction.ZERO;
        }

        StringBuilder buf = new StringBuilder();

        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(0);
        f.setGroupingUsed(false);
        if (f instanceof DecimalFormat) {
            ((DecimalFormat) f).setDecimalSeparatorAlwaysShown(false);
        }

        int num = stopsValue.getNumerator();
        int den = stopsValue.getDenominator();
        int whole = num / den;
        num %= den;

        if (num > 0 || whole > 0) {
            buf.append('+');
        } else if (num < 0 || whole < 0) {
            buf.append('-');
        }
        whole = FastMath.abs(whole);
        num = FastMath.abs(num);

        if (whole > 0) {
            buf.append(f.format(whole));
        }

        if (num > 0) {
            if (whole > 0) {
                buf.append(' ');
                buf.append(num);
                buf.append('\u2044');
                buf.append(den);
            } else {
                buf.append(f.format(num));
                buf.append("/");
                buf.append(f.format(den));
            }
        }

        if (buf.length() == 0) {
            buf.append('0');
        }

        return buf.toString();
    }
}
