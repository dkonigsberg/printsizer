package org.logicprobe.printsizer.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;

import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.model.ExposureAdjustment;
import org.logicprobe.printsizer.model.Fraction;

public class BurnDodgeDialogViewModel extends AndroidViewModel {
    private static final String TAG = BurnDodgeDialogViewModel.class.getSimpleName();
    private static final String INITIALIZED_KEY = "initialized";
    private static final String ITEM_ID_KEY = "item_id";
    private static final String INDEX_KEY = "index";
    private static final String NAME_KEY = "name";
    private static final String DEFAULT_NAME_KEY = "default_name";
    private static final String FINE_STOP_INC_KEY = "fine_stop_inc";
    private static final String COARSE_STOP_INC_KEY = "coarse_stop_inc";
    private static final String ADJ_MODE_KEY = "adj_mode";
    private static final String SECONDS_VALUE_KEY = "seconds_value";
    private static final String PERCENT_VALUE_KEY = "percent_value";
    private static final String STOPS_VALUE_KEY = "stops_value";
    private static final String BASE_EXPOSURE_TIME_KEY = "base_exposure_time";
    private static final String HAS_VALUE_KEY = "has_value";
    private static final String LAST_USER_ADJ_VALUE = "user_adj_value";

    private static final Fraction FINE_STOP_INC_DEFAULT = new Fraction(1, 12);
    private static final Fraction COARSE_STOP_INC_DEFAULT = Fraction.ONE;

    private final SavedStateHandle state;

    public BurnDodgeDialogViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        state.set(ADJ_MODE_KEY, ExposureAdjustment.UNIT_NONE);
        state.set(SECONDS_VALUE_KEY, 0.0d);
        state.set(PERCENT_VALUE_KEY, 0);
        state.set(STOPS_VALUE_KEY, Fraction.ZERO);
        state.set(HAS_VALUE_KEY, Boolean.FALSE);
        state.set(LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_NONE);
    }

    public void setInitialized(boolean initialized) {
        state.set(INITIALIZED_KEY, initialized);
    }

    public boolean isInitialized() {
        return Util.safeGetStateBoolean(state, INITIALIZED_KEY);
    }

    public void setBurnDodgeItem(BurnDodgeItem burnDodgeItem) {
        state.set(ITEM_ID_KEY, burnDodgeItem.getItemId());
        state.set(INDEX_KEY, burnDodgeItem.getIndex());
        state.set(NAME_KEY, burnDodgeItem.getName());
        state.set(DEFAULT_NAME_KEY,
            burnDodgeItem.getDefaultName(getApplication().getApplicationContext().getResources()));

        ExposureAdjustment adjustment = burnDodgeItem.getAdjustment();
        if (adjustment != null) {
            state.set(SECONDS_VALUE_KEY, adjustment.getSecondsValue());
            state.set(PERCENT_VALUE_KEY, adjustment.getPercentValue());
            if (adjustment.getStopsValue() != null) {
                state.set(STOPS_VALUE_KEY, adjustment.getStopsValue());
            }
            state.set(ADJ_MODE_KEY, adjustment.getUnit());
            state.set(LAST_USER_ADJ_VALUE, adjustment.getUnit());
        }
        updateHasValueState();
    }

    public BurnDodgeItem buildBurnDodgeItem() {
        BurnDodgeItem item = new BurnDodgeItem();
        item.setItemId(Util.safeGetStateLong(state, ITEM_ID_KEY, 0));
        item.setIndex(Util.safeGetStateInt(state, INDEX_KEY, 0));
        item.setName((String)state.get(NAME_KEY));
        ExposureAdjustment adjustment = buildExposureAdjustment();
        item.setAdjustment(adjustment);
        return item;
    }

    private ExposureAdjustment buildExposureAdjustment() {
        int adjMode = Util.safeGetStateInt(state, ADJ_MODE_KEY, ExposureAdjustment.UNIT_STOPS);
        return buildExposureAdjustment(adjMode);
    }

    private ExposureAdjustment buildExposureAdjustment(@ExposureAdjustment.AdjustmentUnit int adjMode) {
        ExposureAdjustment adjustment = new ExposureAdjustment();
        switch (adjMode) {
            case ExposureAdjustment.UNIT_PERCENT:
                adjustment.setPercentValue(Util.safeGetStateInt(state, PERCENT_VALUE_KEY, 0));
                break;
            case ExposureAdjustment.UNIT_SECONDS:
                adjustment.setSecondsValue(Util.safeGetStateDouble(state, SECONDS_VALUE_KEY, 0d));
                break;
            case ExposureAdjustment.UNIT_STOPS:
                adjustment.setStopsValue((Fraction) state.get(STOPS_VALUE_KEY));
                break;
            case ExposureAdjustment.UNIT_NONE:
            default:
                adjustment.setStopsValue(Fraction.ZERO);
                break;
        }
        return adjustment;
    }

    public void setName(String name) {
        state.set(NAME_KEY, name);
    }

    public LiveData<String> getName() {
        return state.getLiveData(NAME_KEY);
    }

    public void setDefaultName(String defaultName) {
        state.set(DEFAULT_NAME_KEY, defaultName);
    }

    public LiveData<String> getDefaultName() {
        return state.getLiveData(DEFAULT_NAME_KEY);
    }

    public void setFineStopIncrement(Fraction increment) {
        state.set(FINE_STOP_INC_KEY, increment);
    }

    public void setCoarseStopIncrement(Fraction increment) {
        state.set(COARSE_STOP_INC_KEY, increment);
    }

    public void setAdjustmentMode(@ExposureAdjustment.AdjustmentUnit int mode) {
        int adjMode = Util.safeGetStateInt(state, LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_STOPS);
        ExposureAdjustment prevAdjustment = buildExposureAdjustment(adjMode);
        double baseExposureTime = Util.safeGetStateDouble(state, BASE_EXPOSURE_TIME_KEY, 0.0d);
        ExposureAdjustment nextAdjustment = prevAdjustment.convertTo(mode, baseExposureTime);

        state.set(ADJ_MODE_KEY, mode);

        // If converting to stops, make sure to twiddle our fractional result so that it only uses
        // a denominator that we can currently set via the UI.
        if (nextAdjustment != null &&
                prevAdjustment.getUnit() != ExposureAdjustment.UNIT_STOPS && nextAdjustment.getUnit() == ExposureAdjustment.UNIT_STOPS) {
            int coarseDenominator = Util.safeGetStateFraction(state, COARSE_STOP_INC_KEY, COARSE_STOP_INC_DEFAULT).getDenominator();
            int fineDenominator = Util.safeGetStateFraction(state, FINE_STOP_INC_KEY, FINE_STOP_INC_DEFAULT).getDenominator();
            Fraction stopsFraction = nextAdjustment.getStopsValue();
            if (stopsFraction.getDenominator() != coarseDenominator && stopsFraction.getDenominator() != fineDenominator) {
                Fraction constrainedFraction = Util.buildConstrainedStopsFraction(
                        stopsFraction.doubleValue(), new int[] { coarseDenominator, fineDenominator });
                if (constrainedFraction != null) {
                    nextAdjustment.setStopsValue(constrainedFraction);
                }
            }
        }

        if (nextAdjustment != null && nextAdjustment.getUnit() == mode) {
            switch (mode) {
                case ExposureAdjustment.UNIT_SECONDS:
                    state.set(SECONDS_VALUE_KEY, nextAdjustment.getSecondsValue());
                    break;
                case ExposureAdjustment.UNIT_PERCENT:
                    state.set(PERCENT_VALUE_KEY, nextAdjustment.getPercentValue());
                    break;
                case ExposureAdjustment.UNIT_STOPS:
                    state.set(STOPS_VALUE_KEY, nextAdjustment.getStopsValue());
                    break;
                case ExposureAdjustment.UNIT_NONE:
                default:
                    break;
            }
        }

        updateHasValueState();
    }

    public LiveData<Integer> getAdjustmentMode() {
        return state.getLiveData(ADJ_MODE_KEY, ExposureAdjustment.UNIT_STOPS);
    }

    public void setSecondsValue(double secondsValue) {
        Double previous = state.get(SECONDS_VALUE_KEY);
        state.set(SECONDS_VALUE_KEY, secondsValue);
        if (previous == null || !Util.isEqual(previous, secondsValue)) {
            state.set(LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_SECONDS);
        }
        updateHasValueState();
    }

    public LiveData<Double> getSecondsValue() {
        return state.getLiveData(SECONDS_VALUE_KEY, 0.0d);
    }

    public void setPercentValue(int percentValue) {
        Integer previous = state.get(PERCENT_VALUE_KEY);
        state.set(PERCENT_VALUE_KEY, percentValue);
        if (previous == null || !Util.isEqual(previous, percentValue)) {
            state.set(LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_PERCENT);
        }
        updateHasValueState();
    }

    public LiveData<Integer> getPercentValue() {
        return state.getLiveData(PERCENT_VALUE_KEY, 0);
    }

    public void setStopsValue(Fraction stopsValue) {
        Fraction previous = state.get(STOPS_VALUE_KEY);
        state.set(STOPS_VALUE_KEY, stopsValue);
        if (previous == null || !previous.equals(stopsValue)) {
            state.set(LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_STOPS);
        }
        updateHasValueState();
    }

    public void incrementStopsValueCoarse() {
        Fraction adjustment = Util.safeGetStateFraction(state, COARSE_STOP_INC_KEY, COARSE_STOP_INC_DEFAULT);
        adjustStopsValue(adjustment);
    }

    public void decrementStopsValueCoarse() {
        Fraction adjustment = Util.safeGetStateFraction(state, COARSE_STOP_INC_KEY, COARSE_STOP_INC_DEFAULT);
        adjustStopsValue(adjustment.negate());
    }

    public void incrementStopsValueFine() {
        Fraction adjustment = Util.safeGetStateFraction(state, FINE_STOP_INC_KEY, FINE_STOP_INC_DEFAULT);
        adjustStopsValue(adjustment);
    }

    public void decrementStopsValueFine() {
        Fraction adjustment = Util.safeGetStateFraction(state, FINE_STOP_INC_KEY, FINE_STOP_INC_DEFAULT);
        adjustStopsValue(adjustment.negate());
    }

    private void adjustStopsValue(Fraction adjustment) {
        Fraction stopsValue = state.get(STOPS_VALUE_KEY);
        if (stopsValue == null) {
            stopsValue = Fraction.ZERO;
        }
        Fraction resultValue = Util.constrainedFractionAdd(stopsValue, adjustment);

        state.set(STOPS_VALUE_KEY, resultValue);
        state.set(LAST_USER_ADJ_VALUE, ExposureAdjustment.UNIT_STOPS);
        updateHasValueState();
    }

    public LiveData<Fraction> getStopsValue() {
        return state.getLiveData(STOPS_VALUE_KEY, Fraction.ZERO);
    }

    public void setBaseExposureTime(double baseExposureTime) {
        state.set(BASE_EXPOSURE_TIME_KEY, baseExposureTime);
    }

    public LiveData<Boolean> hasValue() {
        return state.getLiveData(HAS_VALUE_KEY);
    }

    private void updateHasValueState() {
        int adjMode = Util.safeGetStateInt(state, ADJ_MODE_KEY, ExposureAdjustment.UNIT_NONE);
        boolean hasValue;
        if (adjMode == ExposureAdjustment.UNIT_SECONDS) {
            double value = Util.safeGetStateDouble(state, SECONDS_VALUE_KEY, 0.0d);
            hasValue = Util.isValidNonZero(value);
        } else if (adjMode == ExposureAdjustment.UNIT_PERCENT) {
            int value = Util.safeGetStateInt(state, PERCENT_VALUE_KEY, 0);
            hasValue = Math.abs(value) > 0;
        } else if (adjMode == ExposureAdjustment.UNIT_STOPS) {
            Fraction value = state.get(STOPS_VALUE_KEY);
            hasValue = !Fraction.ZERO.equals(value);
        } else {
            hasValue = false;
        }
        state.set(HAS_VALUE_KEY, hasValue);
    }
}
