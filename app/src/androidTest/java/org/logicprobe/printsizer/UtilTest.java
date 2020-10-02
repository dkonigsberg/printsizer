package org.logicprobe.printsizer;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.logicprobe.printsizer.model.Fraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class UtilTest {
    private static double EPSILON = 0.001d;

    @Test
    public void buildConstrainedStopsFractionFromInvalid() {
        double stopsValue = Double.NaN;
        Fraction fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertNull(fraction);

        stopsValue = Double.POSITIVE_INFINITY;
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertNull(fraction);
    }

    @Test
    public void buildConstrainedStopsFractionSimple() {
        double stopsValue = 1;
        Fraction fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.ONE, fraction);

        stopsValue = 2;
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.TWO, fraction);

        stopsValue = 0.25;
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.ONE_QUARTER, fraction);

        stopsValue = 1.25;
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(new Fraction(5, 4), fraction);

        stopsValue = 0.33;
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.ONE_THIRD, fraction);
    }

    @Test
    public void buildConstrainedStopsFractionClosest() {
        double stopsValue = 0.2; // 1/5th
        Fraction fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.getUnreducedFraction(5, 24), fraction);

        stopsValue = 0.142857; // ~1/7th
        fraction = Util.buildConstrainedStopsFraction(stopsValue);
        assertEquals(Fraction.getUnreducedFraction(3, 24), fraction);
    }

    @Test
    public void buildConstrainedStopsFractionLimited() {
        int[] constraintList = new int[] { 1, 4 };

        double stopsValue = 0.2; // 1/5th
        Fraction fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.getUnreducedFraction(1, 4), fraction);

        stopsValue = 1.2; // 6/5th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.getUnreducedFraction(5, 4), fraction);

        stopsValue = 0.142857; // ~1/7th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.getUnreducedFraction(1, 4), fraction);

        stopsValue = 1.142857; // ~8/7th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.getUnreducedFraction(5, 4), fraction);

        stopsValue = 0.041667; // ~1/24th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.ZERO, fraction);

        stopsValue = 1.041667; // ~25/24th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.ONE, fraction);
    }

    @Test
    public void buildConstrainedStopsFractionImplicitDenominator() {
        int[] constraintList = new int[] { 2, 4 };

        double stopsValue = 1.041667; // ~25/24th
        Fraction fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.ONE, fraction);

        stopsValue = 0.041667; // ~1/24th
        fraction = Util.buildConstrainedStopsFraction(stopsValue, constraintList);
        assertEquals(Fraction.ZERO, fraction);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void convertFractionToDenominator() {
        // Null case
        Fraction fraction = Util.convertFractionToDenominator(null, 0);
        assertNull(fraction);

        // Matching case
        fraction = Util.convertFractionToDenominator(Fraction.ONE_QUARTER, 4);
        assertNotNull(fraction);
        assertEquals(Fraction.ONE_QUARTER, fraction);
        assertEquals(1, fraction.getNumerator());
        assertEquals(4, fraction.getDenominator());

        // Simple conversion case
        fraction = Util.convertFractionToDenominator(Fraction.ONE_QUARTER, 8);
        assertNotNull(fraction);
        assertNotEquals(Fraction.ONE_QUARTER, fraction);
        assertEquals(0.25, fraction.doubleValue(), EPSILON);
        assertEquals(2, fraction.getNumerator());
        assertEquals(8, fraction.getDenominator());
    }
}
