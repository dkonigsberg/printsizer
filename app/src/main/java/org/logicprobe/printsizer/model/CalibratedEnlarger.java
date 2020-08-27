package org.logicprobe.printsizer.model;

import android.util.Log;

import org.logicprobe.printsizer.Util;

public class CalibratedEnlarger extends Enlarger {
    private static final String TAG = CalibratedEnlarger.class.getSimpleName();

    private final double smallerTestDistance;
    private double smallerTestMagnification;
    private final double smallerTestTime;

    private double middleDistance;
    private double middleMagnification;
    private double middleTime;

    private final double largerTestDistance;
    private double largerTestMagnification;
    private final double largerTestTime;

    public CalibratedEnlarger(double smallerTestDistance, double smallerTestTime,
                    double largerTestDistance, double largerTestTime,
                    double lensFocalLength) {
        super(lensFocalLength);

        if (!Util.isValidPositive(smallerTestDistance) || !Util.isValidPositive(smallerTestTime)
                || !Util.isValidPositive(largerTestDistance)|| !Util.isValidPositive(largerTestTime)) {
            throw new IllegalArgumentException("Test exposure values must be positive numbers");
        }
        if (smallerTestDistance >= largerTestDistance) {
            throw new IllegalArgumentException("Invalid test distance relationship");
        }
        if (smallerTestTime >= largerTestTime) {
            throw new IllegalArgumentException("Invalid test time relationship");
        }
        if (smallerTestDistance < PrintMath.computeMinimumHeight(lensFocalLength)) {
            throw new IllegalArgumentException("smallerTestDistance is too low for the lens focal length");
        }

        this.smallerTestDistance = smallerTestDistance;
        this.smallerTestTime = smallerTestTime;
        this.largerTestDistance = largerTestDistance;
        this.largerTestTime = largerTestTime;

        calculateCurveParameters();
    }

    private void calculateCurveParameters() {
        calculateTestMagnifications();
        calculateMidpointIdealOffsetMatch();
        logCurveParameters();
    }

    /**
     * Calculate the print magnification values for both test exposures.
     *
     * These are simple transformations of the input parameters, so that the rest
     * of the calculations that work entirely with magnification values instead of distance.
     */
    private void calculateTestMagnifications() {
        // Compute the print magnification values for both profile parameters
        smallerTestMagnification = PrintMath.computeMagnification(smallerTestDistance, lensFocalLength);
        largerTestMagnification = PrintMath.computeMagnification(largerTestDistance, lensFocalLength);
    }

    /**
     * Calculate the midpoint of the profile curve, using an average of two ideal curves.
     *
     * This approach uses the ideal exposure curve, starting from each of the test exposure,
     * endpoints, and averages their middle to produce a third point for the profile.
     * It works well with enlargers that do not deviate much from the ideal curves, but produces
     * something closer to a straight line output when the enlarger does deviate.
     *
     * Note: This algorithm is no longer used, but the code will remain here until we're absolutely
     *       certain that the new algorithm is an accepted replacement.
     */
    private void calculateMidpointIdealAverage() {
        // Compute the print height and magnification for a midpoint between the two profile parameters
        middleDistance = (smallerTestDistance + largerTestDistance) / 2;
        middleMagnification = PrintMath.computeMagnification(middleDistance, lensFocalLength);

        // Determine the print time for the midpoint height by extrapolating from both the smaller
        // and larger values using the standard print scaling formula, and averaging the results.
        middleTime = (PrintMath.computeTimeAdjustment(smallerTestTime, smallerTestMagnification, middleMagnification)
                + PrintMath.computeTimeAdjustment(largerTestTime, largerTestMagnification, middleMagnification)) / 2;
    }

    /**
     * Calculate the midpoint of the profile curve, by matching the bend of the ideal curve.
     *
     * This approach attempts to measure the bend of the ideal curve, at the midpoint of a straight
     * line that intersects it at the test magnifications. It then applies that measurement to
     * the line formed by the test exposures, to create a more accurate profile curve midpoint.
     */
    private void calculateMidpointIdealOffsetMatch() {
        // Logically, we're operating on a 2-dimensional graph where:
        // x-axis: magnification
        // y-axis: exposure time
        // As far as terminology, this code will refer to the smaller test as the "low" point
        // and the larger test as the "high" point. Furthermore, "magnification" will be shortened
        // to "mag". This is being done to shorten and simplify variable names, which would
        // otherwise get so long and confusing that they might obscure their meaning.

        // Define new variable names for brevity and consistency
        final double lowMag = smallerTestMagnification;
        final double lowTime = smallerTestTime;
        final double highMag = largerTestMagnification;
        final double highTime = largerTestTime;

        // Define the ideal curve, starting from the low end
        final double idealLowTime = smallerTestTime;
        final double idealHighTime = PrintMath.computeTimeAdjustment(lowTime, lowMag, highMag);

        // Find the midpoint of a straight line that intersects the ideal curve at the low
        // and high magnification reference points
        final double idealLineMidMag = (lowMag + highMag) / 2d;
        final double idealLineMidTime = (idealLowTime + idealHighTime) / 2d;

        // Find the slope of a line that is perpendicular to the ideal line
        final double idealLinePerpSlope = -1d * ((highMag - lowMag) / (idealHighTime - idealLowTime));

        // Find the point at which this perpendicular line intercepts the ideal curve
        final double idealCurveInterceptMag =
            -1d * ((Math.pow(lowMag + 1d, 2d) * (
                -1d * Math.sqrt(
                    -1d * ((4d * idealLineMidMag * idealLinePerpSlope * lowTime) / Math.pow(lowMag + 1d, 2d))
                        + (4d * idealLineMidTime * lowTime) / Math.pow(lowMag + 1d, 2d)
                        - (4d * idealLinePerpSlope * lowTime) / Math.pow(lowMag + 1d, 2d)
                        + Math.pow(idealLinePerpSlope, 2d)
                )
                    + (2d * lowTime) / Math.pow(lowMag + 1d, 2d) - idealLinePerpSlope
            )) / (2d * lowTime));

        final double idealCurveInterceptTime = PrintMath.computeTimeAdjustment(lowTime, lowMag, idealCurveInterceptMag);

        Log.d(TAG, "idealCurveInterceptMag = " + idealCurveInterceptMag);
        Log.d(TAG, "idealCurveInterceptTime = " + idealCurveInterceptTime);

        // Find the length of the segment of this perpendicular line that intercepts both the
        // ideal line and the ideal curve
        final double perpSegmentLength = Math.sqrt(
                Math.pow(idealCurveInterceptMag - idealLineMidMag, 2d)
                        + Math.pow(idealCurveInterceptTime - idealLineMidTime, 2d));

        Log.d(TAG, "perpSegmentLength = " + perpSegmentLength);

        // Find the midpoint of the straight-line from the low magnification to high magnification
        // test exposures
        final double testLineMidMag = (lowMag + highMag) / 2d;
        final double testLineMidTime = (lowTime + highTime) / 2d;

        // Find the perpendicular slope of this line
        final double testLinePerpSlope = -1d * ((highMag - lowMag) / (highTime - lowTime));

        // Find the point which is offset from the test line midpoint, along the perpendicular,
        // at a distance determined by the above-calculated ideal perpendicular segment length
        final double offsetSqrtTerm = Math.sqrt(1d / (1d + Math.pow(testLinePerpSlope, 2d)));
        final double offsetMidMag = testLineMidMag + perpSegmentLength
                * offsetSqrtTerm;
        final double offsetMidTime = testLineMidTime + testLinePerpSlope * perpSegmentLength
                * offsetSqrtTerm;

        Log.d(TAG, "offsetMidMag = " + offsetMidMag);
        Log.d(TAG, "offsetMidTime = " + offsetMidTime);

        // Assign class members for the midpoint based on these calculations
        this.middleDistance = PrintMath.computeHeightFromMagnification(offsetMidMag, lensFocalLength);
        this.middleMagnification = offsetMidMag;
        this.middleTime = offsetMidTime;
    }

    private void logCurveParameters() {
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

    public double profileExposureTime(double enlargerHeight) {
        if (enlargerHeight < PrintMath.computeMinimumHeight(lensFocalLength)) {
            throw new IllegalArgumentException("enlargerHeight is too low for the lens focal length");
        }

        final double magnification = PrintMath.computeMagnification(enlargerHeight, lensFocalLength);
        return PrintMath.interpolate(
                smallerTestMagnification, smallerTestTime,
                middleMagnification, middleTime,
                largerTestMagnification, largerTestTime,
                magnification);
    }
}
