package org.logicprobe.printsizer;

import androidx.lifecycle.LiveData;

public class LiveDataUtil {
    private LiveDataUtil() {
    }

    public static double getDoubleValue(final LiveData<Double> liveData) {
        Double value = liveData.getValue();
        return (value != null) ? value : Double.NaN;
    }

    public static int getIntValue(final LiveData<Integer> liveData) {
        Integer value = liveData.getValue();
        return (value != null) ? value : 0;
    }
}
