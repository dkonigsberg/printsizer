package org.logicprobe.printsizer.model;

public final class PrintMath {
    private PrintMath() {
        throw new UnsupportedOperationException();
    }

    public static double computeMinimumHeight(double lensFocalLength) {
        return 4.0d * lensFocalLength;
    }

    public static double computeMagnification(double enlargerHeight, double lensFocalLength) {
        return (Math.sqrt(enlargerHeight * (enlargerHeight - 4 * lensFocalLength)) - 2 * lensFocalLength + enlargerHeight) / (2 * lensFocalLength);
    }

    public static double computeTimeAdjustment(double oldTime, double oldMagnification, double newMagnification) {
        return oldTime * Math.pow(newMagnification + 1, 2) / Math.pow(oldMagnification + 1, 2);
    }

    public static double interpolate(double h1, double t1, double h2, double t2, double h3, double t3, double x) {
        return (t1 * ((x - h2) / (h1 - h2)) * ((x - h3) / (h1 - h3))) +
                (t2 * ((x - h1) / (h2 - h1)) * ((x - h3) / (h2 - h3))) +
                (t3 * ((x - h1) / (h3 - h1)) * ((x - h2) / (h3 - h2)));
    }

    public static double isoPaperDifferenceInEv(double isoP1, double isoP2) {
        return Math.log(isoP1 / isoP2) / Math.log(2.0d);
    }

    public static double timeAdjustInStops(double time, double stops) {
        return time * Math.pow(2, stops);
    }
}
