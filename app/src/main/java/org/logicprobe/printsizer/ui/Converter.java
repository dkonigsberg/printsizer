package org.logicprobe.printsizer.ui;

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
}
