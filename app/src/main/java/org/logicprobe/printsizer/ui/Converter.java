package org.logicprobe.printsizer.ui;

import androidx.annotation.StringRes;

import org.logicprobe.printsizer.R;
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
}
