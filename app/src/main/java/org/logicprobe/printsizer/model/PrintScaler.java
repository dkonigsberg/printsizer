package org.logicprobe.printsizer.model;

import android.util.Log;

/**
 * PrintScaler is the central class for calculating printing time adjustments.
 *
 * The intended usage is to set all the relevant properties, then call {@link #calculateTargetExposureTime()}
 * to get an adjusted printing time. The required properties are the enlarger, the
 * base height and exposure time, and the target height. All other properties are optional.
 *
 * With the exception of the {@link Enlarger} representation, the parameters are
 * provided as primitive values. This is because they come from a variety of
 * sources, scattered across a variety of objects.
 */
public class PrintScaler {
    private static final String TAG = PrintScaler.class.getSimpleName();
    private static final double EPSILON = 0.0001d;
    private Enlarger enlarger;
    private double baseHeight;
    private double baseExposureTime;
    private double baseIsoP;
    private double targetHeight;
    private double targetIsoP;
    private double exposureCompensation;

    public PrintScaler(Enlarger enlarger) {
        this.enlarger = enlarger;
    }

    public void setBaseHeight(double baseHeight) {
        this.baseHeight = baseHeight;
    }

    public void setBaseExposureTime(double baseExposureTime) {
        this.baseExposureTime = baseExposureTime;
    }

    public void setBaseIsoP(double baseIsoP) {
        this.baseIsoP = baseIsoP;
    }

    public void setTargetHeight(double targetHeight) {
        this.targetHeight = targetHeight;
    }

    public void setTargetIsoP(double targetIsoP) {
        this.targetIsoP = targetIsoP;
    }

    public void setExposureCompensation(double exposureCompensation) {
        this.exposureCompensation = exposureCompensation;
    }

    public double calculateTargetExposureTime() {
        Log.d(TAG, "Compute target exposure time: " +
                "(" + baseHeight + "mm, " + baseExposureTime + "s) -> " + targetHeight + "mm");

        if (enlarger == null || !isValidPositive(enlarger.getLensFocalLength())) {
            Log.e(TAG, "Enlarger is missing or invalid");
            return Double.NaN;
        }

        if (!(isValidPositive(baseHeight) && isValidPositive(baseExposureTime) && isValidPositive(targetHeight))) {
            Log.e(TAG, "Required parameter is missing!");
            return Double.NaN;
        }

        double baseMagnification = PrintMath.computeMagnification(baseHeight, enlarger.getLensFocalLength());
        double targetMagnification = PrintMath.computeMagnification(targetHeight, enlarger.getLensFocalLength());

        double targetCalculatedExposureTime = PrintMath.computeTimeAdjustment(
                baseExposureTime, baseMagnification, targetMagnification);

        Log.d(TAG, "Calculated target time (" + targetHeight + "mm) = " + targetCalculatedExposureTime + "s");

        double targetExposureTime;
        if (enlarger instanceof CalibratedEnlarger) {
            CalibratedEnlarger calibratedEnlarger = (CalibratedEnlarger)enlarger;
            double profileBaseTime = calibratedEnlarger.profileExposureTime(baseHeight);
            double profileTargetTime = calibratedEnlarger.profileExposureTime(targetHeight);

            Log.d(TAG, "Curve base time (" + baseHeight + "mm) = " + profileBaseTime + "s");
            Log.d(TAG, "Curve target time (" + targetHeight + "mm) = " + profileTargetTime + "s");

            double targetProfileExposureTime = (baseExposureTime/profileBaseTime) * profileTargetTime;

            Log.d(TAG, "Profile target time (" + targetHeight + "mm) = " + targetProfileExposureTime + "s");

            targetExposureTime = targetProfileExposureTime;
        } else {
            targetExposureTime = targetCalculatedExposureTime;
        }

        if (hasPaperIsoChange()) {
            double stops = PrintMath.isoPaperDifferenceInEv(baseIsoP, targetIsoP);
            Log.d(TAG, "Paper ISO(P) (" + baseIsoP + "->" + targetIsoP + ") adjustment: " + stops + " EV");
            targetExposureTime = PrintMath.timeAdjustInStops(targetExposureTime, stops);
            Log.d(TAG, "Paper adjusted target exposure: " + targetExposureTime + "s");
        }

        if (isValidNonZero(exposureCompensation)) {
            Log.d(TAG, "Exposure compensation: " + exposureCompensation + " EV");
            targetExposureTime = PrintMath.timeAdjustInStops(targetExposureTime, exposureCompensation);
            Log.d(TAG, "Compensated target exposure: " + targetExposureTime + "s");
        }

        return targetExposureTime;
    }

    private boolean hasPaperIsoChange() {
        return isValidPositive(baseIsoP) && isValidPositive(targetIsoP)
                && Math.abs(baseIsoP - targetIsoP) > EPSILON;
    }

    private static boolean isValidNonZero(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && Math.abs(value) > EPSILON;
    }

    private static boolean isValidPositive(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value > EPSILON;
    }
}
