package org.logicprobe.printsizer.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PrintScalerTest {
    private static double EPSILON = 0.001d;
    private static double HEIGHT_50MM_1X = 200.0d;
    private static double HEIGHT_50MM_10X = 605.0d;

    @Test
    public void basicUncalibratedEnlargement() {
        Enlarger enlarger = new Enlarger(50);
        PrintScaler printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);

        double targetTime = printScaler.calculateTargetExposureTime();
        assertEquals(302.5d, targetTime, EPSILON);
    }

    @Test
    public void scaleInvalidBaseHeight() {
        Enlarger enlarger = new Enlarger(50);
        PrintScaler printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X - 10.0d);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);

        double targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));
    }

    @Test
    public void scaleInvalidTargetHeight() {
        Enlarger enlarger = new Enlarger(50);
        PrintScaler printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_10X);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_1X - 10.0d);

        double targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));
    }

    @Test
    public void missingEnlarger() {
        PrintScaler printScaler = new PrintScaler(null);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);

        double targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));
    }

    @Test()
    public void invalidEnlarger() {
        // Force an apparently invalid enlarger, because validation checks prevent
        // us from easily constructing one.
        Enlarger enlarger = new Enlarger(50) {
            @Override
            public double getLensFocalLength() {
                return 0;
            }
        };

        PrintScaler printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);

        double targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));
    }

    @Test
    public void scaleMissingParameters() {
        Enlarger enlarger = new Enlarger(50);

        // No parameters
        PrintScaler printScaler = new PrintScaler(enlarger);
        double targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));

        // Just base height
        printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));

        // Just base and target heights
        printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);
        targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));

        // Just base height and time
        printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setBaseExposureTime(10);
        targetTime = printScaler.calculateTargetExposureTime();
        assertTrue(Double.isNaN(targetTime));
    }

    @Test
    public void scaleIdealCalibration() {
        // Use an uncalibrated instance to build some test values, then use those values
        // to construct a calibrated instance for testing.
        Enlarger enlarger = new Enlarger(50);
        PrintScaler printScaler = new PrintScaler(enlarger);

        // First compute the enlarger calibration values
        final double testBaseHeight = HEIGHT_50MM_1X + 10.0d;
        final double testBaseExposure = 10.0d;
        final double testTargetHeight = HEIGHT_50MM_10X - 50.0d;
        printScaler.setBaseHeight(testBaseHeight);
        printScaler.setBaseExposureTime(testBaseExposure);
        printScaler.setTargetHeight(testTargetHeight);
        final double testTargetExposure = printScaler.calculateTargetExposureTime();

        // Then compute an unrelated exposure adjustment for comparison
        printScaler = new PrintScaler(enlarger);
        final double refBaseHeight = HEIGHT_50MM_1X + 50.0d;
        final double refBaseExposure = 20.0d;
        final double refTargetHeight = HEIGHT_50MM_10X - 200.0d;
        printScaler.setBaseHeight(refBaseHeight);
        printScaler.setBaseExposureTime(refBaseExposure);
        printScaler.setTargetHeight(refTargetHeight);
        final double refTargetExposure = printScaler.calculateTargetExposureTime();

        // Now construct a calibrated enlarger based on the "test" data above
        enlarger = new CalibratedEnlarger(
                testBaseHeight, testBaseExposure,
                testTargetHeight, testTargetExposure,
                50);
        // Attempt to use this calibrated enlarger to reproduce the same scaling
        printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(refBaseHeight);
        printScaler.setBaseExposureTime(refBaseExposure);
        printScaler.setTargetHeight(refTargetHeight);
        double calTargetExposure = printScaler.calculateTargetExposureTime();

        assertEquals(refTargetExposure, calTargetExposure, EPSILON);
    }

    @Test
    public void exposureAdjustment() {
        Enlarger enlarger = new Enlarger(50);
        PrintScaler printScaler = new PrintScaler(enlarger);
        printScaler.setBaseHeight(HEIGHT_50MM_1X);
        printScaler.setBaseExposureTime(10.0d);
        printScaler.setTargetHeight(HEIGHT_50MM_10X);

        final double targetTime = printScaler.calculateTargetExposureTime();

        printScaler.setExposureCompensation(1.0d);
        double compTargetTime = printScaler.calculateTargetExposureTime();
        assertEquals(targetTime * 2.0d, compTargetTime, EPSILON);

        printScaler.setExposureCompensation(-1.0d);
        compTargetTime = printScaler.calculateTargetExposureTime();
        assertEquals(targetTime * 0.5d, compTargetTime, EPSILON);

        printScaler.setExposureCompensation(0.0d);

        printScaler.setBaseIsoP(400);
        printScaler.setTargetIsoP(200);
        compTargetTime = printScaler.calculateTargetExposureTime();
        assertEquals(targetTime * 2.0d, compTargetTime, EPSILON);

        printScaler.setBaseIsoP(200);
        printScaler.setTargetIsoP(400);
        compTargetTime = printScaler.calculateTargetExposureTime();
        assertEquals(targetTime * 0.5d, compTargetTime, EPSILON);
    }
}
