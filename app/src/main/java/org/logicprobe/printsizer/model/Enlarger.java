package org.logicprobe.printsizer.model;

public class Enlarger {
    private static final String TAG = Enlarger.class.getSimpleName();
    protected double lensFocalLength;

    public Enlarger(double lensFocalLength) {
        this.lensFocalLength = lensFocalLength;

        //TODO validate input parameters for acceptable values
    }

    public static Enlarger createFromProfile(EnlargerProfile enlargerProfile) {
        if (enlargerProfile.hasTestExposures()) {
            double offset = enlargerProfile.getHeightMeasurementOffset();
            if (Double.isNaN(offset) || Double.isInfinite(offset)) {
                offset = 0;
            }

            return new CalibratedEnlarger(
                    enlargerProfile.getSmallerTestDistance() + offset,
                    enlargerProfile.getSmallerTestTime(),
                    enlargerProfile.getLargerTestDistance() + offset,
                    enlargerProfile.getLargerTestTime(),
                    enlargerProfile.getLensFocalLength());
        } else {
            return new Enlarger(enlargerProfile.getLensFocalLength());
        }
    }

    public double getLensFocalLength() {
        return lensFocalLength;
    }
}
