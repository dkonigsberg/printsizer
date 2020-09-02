package org.logicprobe.printsizer.model;

public final class PrintMath {
    private PrintMath() {
        throw new UnsupportedOperationException();
    }

    public static double computeMinimumHeight(double lensFocalLength) {
        return 4d * lensFocalLength;
    }

    public static double computeMagnification(double enlargerHeight, double lensFocalLength) {
        return (Math.sqrt(enlargerHeight * (enlargerHeight - 4d * lensFocalLength)) - 2d * lensFocalLength + enlargerHeight) / (2d * lensFocalLength);
    }

    public static double computeHeightFromMagnification(double magnification, double lensFocalLength) {
        return (lensFocalLength * Math.pow(magnification + 1, 2d)) / magnification;
    }

    public static double computeTimeAdjustment(double oldTime, double oldMagnification, double newMagnification) {
        return oldTime * Math.pow(newMagnification + 1d, 2d) / Math.pow(oldMagnification + 1d, 2d);
    }

    public static double interpolate(double h1, double t1, double h2, double t2, double h3, double t3, double x) {
        return (t1 * ((x - h2) / (h1 - h2)) * ((x - h3) / (h1 - h3))) +
                (t2 * ((x - h1) / (h2 - h1)) * ((x - h3) / (h2 - h3))) +
                (t3 * ((x - h1) / (h3 - h1)) * ((x - h2) / (h3 - h2)));
    }

    public static double isoPaperDifferenceInEv(double isoP1, double isoP2) {
        return Math.log(isoP1 / isoP2) / Math.log(2d);
    }

    public static double timeAdjustInStops(double time, double stops) {
        return time * Math.pow(2d, stops);
    }

    public static double timeDifferenceInStops(double t1, double t2) {
        return (Math.log(t2) - Math.log(t1)) / Math.log(2d);
    }
}
