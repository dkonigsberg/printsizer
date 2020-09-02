package org.logicprobe.printsizer.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.math3.fraction.Fraction;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class ExposureAdjustmentTest {
    private static double EPSILON = 0.001d;

    @Test
    public void initialEmptyState() {
        ExposureAdjustment emptyAdjustment = new ExposureAdjustment();
        assertEquals(ExposureAdjustment.UNIT_NONE, emptyAdjustment.getUnit());
        assertEquals(0.0d, emptyAdjustment.getSecondsValue(), EPSILON);
        assertEquals(0, emptyAdjustment.getPercentValue());
        assertEquals(Fraction.ZERO, emptyAdjustment.getStopsValue());
    }

    @Test
    public void convertFromSeconds() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setSecondsValue(10);

        // Converting to our current unit should yield a copy of our instance
        ExposureAdjustment secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 0);
        assertNotSame(adjustment, secondsAdjustment);
        assertEquals(adjustment, secondsAdjustment);

        // Converting to a percent requires a base exposure time
        ExposureAdjustment percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 0);
        assertNull(percentAdjustment);

        // Adjusting a 10 second exposure by 10 seconds should be +100%
        percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 10);
        assertNotNull(percentAdjustment);
        assertEquals(ExposureAdjustment.UNIT_PERCENT, percentAdjustment.getUnit());
        assertEquals(100, percentAdjustment.getPercentValue());

        // Converting to stops requires a base exposure time
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 0);
        assertNull(stopsAdjustment);

        // Adjusting a 10 second exposure by 10 seconds should be 1 stop
        stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 10);
        assertNotNull(stopsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_STOPS, stopsAdjustment.getUnit());
        assertEquals(Fraction.ONE, stopsAdjustment.getStopsValue());
    }

    @Test
    public void convertFromSecondsNegative() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setSecondsValue(-5);

        // Adjusting a 10 second exposure by -5 seconds should be -50%
        ExposureAdjustment percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 10);
        assertNotNull(percentAdjustment);
        assertEquals(ExposureAdjustment.UNIT_PERCENT, percentAdjustment.getUnit());
        assertEquals(-50, percentAdjustment.getPercentValue());

        // Adjusting a 10 second exposure by -5 seconds should be -1 stop
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 10);
        assertNotNull(stopsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_STOPS, stopsAdjustment.getUnit());
        assertEquals(Fraction.ONE.negate(), stopsAdjustment.getStopsValue());
    }

    @Test
    public void convertFromSecondsInvalid() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setSecondsValue(-15);

        // Adjusting a 10 second exposure by -15 seconds has no stops representation
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 10);
        assertNull(stopsAdjustment);
    }

    @Test
    public void convertFromPercent() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setPercentValue(100);

        // Converting to our current unit should yield a copy of our instance
        ExposureAdjustment percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 0);
        assertNotSame(adjustment, percentAdjustment);
        assertEquals(adjustment, percentAdjustment);

        // Converting to seconds requires a base exposure time
        ExposureAdjustment secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 0);
        assertNull(secondsAdjustment);

        // Adjusting a 10 second exposure by 100% should be 10 seconds
        secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 10);
        assertNotNull(secondsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_SECONDS, secondsAdjustment.getUnit());
        assertEquals(10, secondsAdjustment.getSecondsValue(), EPSILON);

        // An adjustment of +100% should be equivalent to 1 stop
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 0);
        assertNotNull(stopsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_STOPS, stopsAdjustment.getUnit());
        assertEquals(Fraction.ONE, stopsAdjustment.getStopsValue());
    }

    @Test
    public void convertFromPercentNegative() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setPercentValue(-50);

        // Adjusting a 10 second exposure by -50% should be a -5 second adjustment
        ExposureAdjustment secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 10);
        assertNotNull(secondsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_SECONDS, secondsAdjustment.getUnit());
        assertEquals(-5, secondsAdjustment.getSecondsValue(), EPSILON);

        // An adjustment of -50% should be equivalent to -1 stop
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 0);
        assertNotNull(stopsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_STOPS, stopsAdjustment.getUnit());
        assertEquals(Fraction.ONE.negate(), stopsAdjustment.getStopsValue());
    }

    @Test
    public void convertFromPercentInvalid() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setPercentValue(-200);

        // -200% has no possible stops representation
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 0);
        assertNull(stopsAdjustment);
    }

    @Test
    public void convertFromStops() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setStopsValue(Fraction.ONE);

        // Converting to our current unit should yield a copy of our instance
        ExposureAdjustment stopsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_STOPS, 0);
        assertNotSame(adjustment, stopsAdjustment);
        assertEquals(adjustment, stopsAdjustment);

        // Converting to seconds requires a base exposure time
        ExposureAdjustment secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 0);
        assertNull(secondsAdjustment);

        // Adjusting a 10 second exposure by 1 stop should be 10 seconds
        secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 10);
        assertNotNull(secondsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_SECONDS, secondsAdjustment.getUnit());
        assertEquals(10, secondsAdjustment.getSecondsValue(), EPSILON);

        // An adjustment of 1 stop should be equivalent to +100%
        ExposureAdjustment percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 0);
        assertNotNull(percentAdjustment);
        assertEquals(ExposureAdjustment.UNIT_PERCENT, percentAdjustment.getUnit());
        assertEquals(100, percentAdjustment.getPercentValue());
    }

    @Test
    public void convertFromStopsNegative() {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        adjustment.setStopsValue(Fraction.ONE.negate());

        // Adjusting a 10 second exposure by -1 stop should be a -5 second adjustment
        ExposureAdjustment secondsAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_SECONDS, 10);
        assertNotNull(secondsAdjustment);
        assertEquals(ExposureAdjustment.UNIT_SECONDS, secondsAdjustment.getUnit());
        assertEquals(-5, secondsAdjustment.getSecondsValue(), EPSILON);

        // An adjustment of -1 stop should be equivalent to -50%
        ExposureAdjustment percentAdjustment = adjustment.convertTo(ExposureAdjustment.UNIT_PERCENT, 0);
        assertNotNull(percentAdjustment);
        assertEquals(ExposureAdjustment.UNIT_PERCENT, percentAdjustment.getUnit());
        assertEquals(-50, percentAdjustment.getPercentValue());
    }
}
