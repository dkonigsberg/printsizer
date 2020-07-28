package org.logicprobe.printsizer.model;

import android.util.Log;

public class Enlarger {
    private static final String TAG = Enlarger.class.getSimpleName();
    private double smallerTestDistance;
    private double smallerTestTime;
    private double largerTestDistance;
    private double largerTestTime;
    private double lensFocalLength;

    private double smallerTestMagnification;
    private double largerTestMagnification;

    private double middleDistance;
    private double middleMagnification;
    private double middleTime;

    public Enlarger(double smallerTestDistance, double smallerTestTime,
                    double largerTestDistance, double largerTestTime,
                    double lensFocalLength) {
        this.smallerTestDistance = smallerTestDistance;
        this.smallerTestTime = smallerTestTime;
        this.largerTestDistance = largerTestDistance;
        this.largerTestTime = largerTestTime;
        this.lensFocalLength = lensFocalLength;

        //TODO validate input parameters for acceptable values

        calculateCurveParameters();
    }

    public Enlarger(EnlargerProfile enlargerProfile) {
        double offset = enlargerProfile.getHeightMeasurementOffset();
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            offset = 0;
        }

        this.smallerTestDistance = enlargerProfile.getSmallerTestDistance() + offset;
        this.smallerTestTime = enlargerProfile.getSmallerTestTime();
        this.largerTestDistance = enlargerProfile.getLargerTestDistance() + offset;
        this.largerTestTime = enlargerProfile.getLargerTestTime();
        this.lensFocalLength = enlargerProfile.getLensFocalLength();

        //TODO validate input parameters for acceptable values

        calculateCurveParameters();
    }

    private void calculateCurveParameters() {
        // Compute the print magnification values for both profile parameters
        smallerTestMagnification = PrintMath.computeMagnification(smallerTestDistance, lensFocalLength);
        largerTestMagnification = PrintMath.computeMagnification(largerTestDistance, lensFocalLength);

        // Compute the print height and magnification for a midpoint between the two profile parameters
        middleDistance = (smallerTestDistance + largerTestDistance) / 2;
        middleMagnification = PrintMath.computeMagnification(middleDistance, lensFocalLength);

        // Determine the print time for the midpoint height by extrapolating from both the smaller
        // and larger values using the standard print scaling formula, and averaging the results.
        middleTime = (PrintMath.computeTimeAdjustment(smallerTestTime, smallerTestMagnification, middleMagnification)
                + PrintMath.computeTimeAdjustment(largerTestTime, largerTestMagnification, middleMagnification)) / 2;

        // Log the input parameters and resulting calculated values
        Log.d(TAG, "Smaller test: " +
                "distance=" + smallerTestDistance + "mm, " +
                "time=" + smallerTestTime + "s, " +
                "magnification=" + smallerTestMagnification);
        Log.d(TAG, "Larger test: " +
                "distance=" + largerTestDistance + "mm, " +
                "time=" + largerTestTime + "s, " +
                "magnification=" + largerTestMagnification);
        Log.d(TAG, "Middle test: " +
                "distance=" + middleDistance + "mm, " +
                "time=" + middleTime + "s, " +
                "magnification=" + middleMagnification);
    }

    public double getLensFocalLength() {
        return lensFocalLength;
    }

    public double profileExposureTime(double enlargerHeight) {
        //TODO validate input parameters for acceptable values
        double magnification = PrintMath.computeMagnification(enlargerHeight, lensFocalLength);
        return PrintMath.interpolate(
                smallerTestMagnification, smallerTestTime,
                middleMagnification, middleTime,
                largerTestMagnification, largerTestTime,
                magnification);
    }
}
