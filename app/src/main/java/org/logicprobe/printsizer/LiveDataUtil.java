package org.logicprobe.printsizer;

import androidx.lifecycle.LiveData;

public class LiveDataUtil {
    private LiveDataUtil() {
    }

    public static double getValue(final LiveData<Double> liveData) {
        Double value = liveData.getValue();
        return (value != null) ? value : Double.NaN;
    }
}
