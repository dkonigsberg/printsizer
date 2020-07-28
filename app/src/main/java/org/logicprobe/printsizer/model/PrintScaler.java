package org.logicprobe.printsizer.model;

import android.util.Log;

public class PrintScaler {
    private static final String TAG = PrintScaler.class.getSimpleName();
    private Enlarger enlarger;
    public PrintScaler(Enlarger enlarger) {
        this.enlarger = enlarger;
    }

    public double scalePrintTime(double baseHeight, double baseExposureTime, double targetHeight) {
        Log.d(TAG, "scalePrintTime(" + baseHeight + "mm, " + baseExposureTime + "s, " + targetHeight + "mm)");

        double profileBaseTime = enlarger.profileExposureTime(baseHeight);
        double profileTargetTime = enlarger.profileExposureTime(targetHeight);

        Log.d(TAG, "Curve base time (" + baseHeight + "mm) = " + profileBaseTime + "s");
        Log.d(TAG, "Curve target time (" + targetHeight + "mm) = " + profileTargetTime + "s");

        double baseMagnification = PrintMath.computeMagnification(baseHeight, enlarger.getLensFocalLength());
        double targetMagnification = PrintMath.computeMagnification(targetHeight, enlarger.getLensFocalLength());

        double targetCalculatedExposureTime = PrintMath.computeTimeAdjustment(
                baseExposureTime, baseMagnification, targetMagnification);
        double targetProfileExposureTime = (baseExposureTime/profileBaseTime) * profileTargetTime;

        Log.d(TAG, "Calculated target time (" + targetHeight + "mm) = " + targetCalculatedExposureTime + "s");
        Log.d(TAG, "Profile target time (" + targetHeight + "mm) = " + targetProfileExposureTime + "s");

        return targetProfileExposureTime;
    }
}
