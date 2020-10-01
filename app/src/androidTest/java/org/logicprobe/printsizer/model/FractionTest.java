package org.logicprobe.printsizer.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class FractionTest {
    private static double EPSILON = 0.001d;

    @Test
    public void unreducedFraction() {
        Fraction reducedFraction = new Fraction(3,24);
        Fraction unreducedFraction = Fraction.getUnreducedFraction(3, 24);
        assertEquals(new Fraction(1, 8), reducedFraction);
        assertNotEquals(new Fraction(1, 8), unreducedFraction);
        assertNotEquals(reducedFraction, unreducedFraction);
        assertEquals(reducedFraction.doubleValue(), unreducedFraction.doubleValue(), EPSILON);
    }
}
