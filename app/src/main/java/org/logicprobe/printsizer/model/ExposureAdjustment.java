package org.logicprobe.printsizer.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import org.apache.commons.math3.fraction.Fraction;
import org.logicprobe.printsizer.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents an exposure adjustment, in several different common units.
 */
public class ExposureAdjustment implements Parcelable {
    public static final int UNIT_NONE = 0;
    public static final int UNIT_SECONDS = 1;
    public static final int UNIT_PERCENT = 2;
    public static final int UNIT_STOPS = 3;

    @IntDef({UNIT_NONE, UNIT_SECONDS, UNIT_PERCENT, UNIT_STOPS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AdjustmentUnit {}

    private int unit;
    private double secondsValue;
    private int percentValue;
    private Fraction stopsValue;

    public ExposureAdjustment() {
        this.unit = UNIT_NONE;
        this.stopsValue = Fraction.ZERO;
    }

    public ExposureAdjustment(ExposureAdjustment adjustment) {
        this.unit = adjustment.unit;
        this.secondsValue = adjustment.secondsValue;
        this.percentValue = adjustment.percentValue;
        this.stopsValue = adjustment.stopsValue;
    }

    public static final Parcelable.Creator<ExposureAdjustment> CREATOR = new Parcelable.Creator<ExposureAdjustment>() {
        @Override
        public ExposureAdjustment createFromParcel(Parcel in) {
            ExposureAdjustment adjustment = new ExposureAdjustment();
            adjustment.unit = in.readInt();
            adjustment.secondsValue = in.readDouble();
            adjustment.percentValue = in.readInt();
            adjustment.stopsValue = (Fraction)in.readSerializable();
            return adjustment;
        }

        @Override
        public ExposureAdjustment[] newArray(int size) {
            return new ExposureAdjustment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(unit);
        out.writeDouble(secondsValue);
        out.writeInt(percentValue);
        out.writeSerializable(stopsValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExposureAdjustment that = (ExposureAdjustment) o;
        return unit == that.unit &&
                Double.compare(that.secondsValue, secondsValue) == 0 &&
                percentValue == that.percentValue &&
                Objects.equals(stopsValue, that.stopsValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unit, secondsValue, percentValue, stopsValue);
    }

    public @AdjustmentUnit int getUnit() {
        return unit;
    }

    public double getSecondsValue() {
        return secondsValue;
    }

    public void setSecondsValue(double secondsValue) {
        this.unit = UNIT_SECONDS;
        this.secondsValue = secondsValue;
    }

    public int getPercentValue() {
        return percentValue;
    }

    public void setPercentValue(int percentValue) {
        this.unit = UNIT_PERCENT;
        this.percentValue = percentValue;
    }

    public Fraction getStopsValue() {
        return stopsValue;
    }

    public void setStopsValue(Fraction stopsValue) {
        this.unit = UNIT_STOPS;
        this.stopsValue = stopsValue;
    }

    /**
     * Convert an exposure adjustment from one unit to another.
     * @param nextUnit The unit to convert to.
     * @param baseExposureTime The exposure time this adjustment is intended to be applied to.
     *                         It is required for conversions to/from seconds, and ignored by other
     *                         conversions.
     * @return A converted adjustment object, or null if the conversion cannot be performed.
     */
    public ExposureAdjustment convertTo(@AdjustmentUnit int nextUnit, double baseExposureTime) {
        if (unit == nextUnit) {
            return new ExposureAdjustment(this);
        }
        if ((unit == UNIT_SECONDS || nextUnit == UNIT_SECONDS) && !Util.isValidPositive(baseExposureTime)) {
            // Converting to/from seconds requires a valid base exposure time
            return null;
        }

        ExposureAdjustment nextAdjustment = null;

        if (unit == UNIT_SECONDS) {
            if (nextUnit == UNIT_PERCENT) {
                nextAdjustment = new ExposureAdjustment();
                nextAdjustment.setPercentValue((int) Math.round(100d * (secondsValue / baseExposureTime)));
            } else if (nextUnit == UNIT_STOPS) {
                nextAdjustment = new ExposureAdjustment();
                double stopsValue = PrintMath.timeDifferenceInStops(baseExposureTime, baseExposureTime + secondsValue);
                if (!Double.isNaN(stopsValue) && !Double.isInfinite(stopsValue)) {
                    nextAdjustment.setStopsValue(new Fraction(stopsValue, 24));
                } else {
                    nextAdjustment = null;
                }
            }
        } else if (unit == UNIT_PERCENT) {
            if (nextUnit == UNIT_SECONDS) {
                nextAdjustment = new ExposureAdjustment();
                nextAdjustment.setSecondsValue((percentValue / 100d) * baseExposureTime);
            } else if (nextUnit == UNIT_STOPS) {
                nextAdjustment = new ExposureAdjustment();
                double multiplier = (percentValue + 100d) / 100d;
                double stops = Math.log(multiplier) / Math.log(2d);
                if (!Double.isNaN(stops) && !Double.isInfinite(stops)) {
                    nextAdjustment.setStopsValue(new Fraction(stops, 24));
                } else {
                    nextAdjustment = null;
                }
            }
        } else if (unit == UNIT_STOPS) {
            if (nextUnit == UNIT_SECONDS) {
                nextAdjustment = new ExposureAdjustment();
                double adjustedTime = PrintMath.timeAdjustInStops(baseExposureTime, stopsValue.doubleValue());
                nextAdjustment.setSecondsValue(adjustedTime - baseExposureTime);
            } else if (nextUnit == UNIT_PERCENT) {
                nextAdjustment = new ExposureAdjustment();
                double multiplier = Math.pow(2, stopsValue.doubleValue());
                nextAdjustment.setPercentValue((int) Math.round((multiplier * 100d) - 100d));
            }
        }

        return nextAdjustment;
    }
}
