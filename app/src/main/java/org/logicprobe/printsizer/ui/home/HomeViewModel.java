package org.logicprobe.printsizer.ui.home;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.model.Enlarger;
import org.logicprobe.printsizer.model.EnlargerProfile;
import org.logicprobe.printsizer.model.PrintMath;
import org.logicprobe.printsizer.model.PrintScaler;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final EnlargerProfile DEFAULT_PROFILE = new EnlargerProfileEntity();
    private static final String DEFAULT_PROFILE_ID_KEY = "default_enlarger_profile_id";
    private static final String SMALLER_PRINT_HEIGHT_KEY = "smaller_print_height";
    private static final String SMALLER_PRINT_EXPOSURE_TIME_KEY = "smaller_print_exposure_time";
    private static final String LARGER_PRINT_HEIGHT_KEY = "larger_print_height";
    private static final String LARGER_PRINT_EXPOSURE_TIME_KEY = "larger_print_exposure_time";
    private static final String ENLARGER_PROFILE_ID_KEY = "enlarger_profile_id";

    private final SavedStateHandle state;
    private final DataRepository repository;

    private MediatorLiveData<EnlargerProfile> enlargerProfile;
    private LiveData<EnlargerProfileEntity> loadedEnlargerProfile;
    private MutableLiveData<EnlargerHeightErrorEvent> smallerPrintHeightError;
    private MutableLiveData<EnlargerHeightErrorEvent> largerPrintHeightError;
    private MutableLiveData<Boolean> enlargerProfileValid;

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        this.repository = ((App)application).getRepository();
        this.enlargerProfile = new MediatorLiveData<>();
        this.smallerPrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.largerPrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.enlargerProfileValid = new MutableLiveData<>(true);

        enlargerProfile.setValue(DEFAULT_PROFILE);
        enlargerProfile.addSource(state.getLiveData(ENLARGER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedEnlargerProfile != null) {
                    enlargerProfile.removeSource(loadedEnlargerProfile);
                    loadedEnlargerProfile = null;
                }
                if (profileId == 0) {
                    enlargerProfile.setValue(DEFAULT_PROFILE);
                    recalculateLargerPrintExposureTime();
                    return;
                }
                loadedEnlargerProfile = repository.loadEnlargerProfile(profileId);
                enlargerProfile.addSource(loadedEnlargerProfile, new Observer<EnlargerProfileEntity>() {
                    @Override
                    public void onChanged(EnlargerProfileEntity enlargerProfileEntity) {
                        if (enlargerProfileEntity == null) {
                            enlargerProfile.setValue(DEFAULT_PROFILE);
                        } else {
                            enlargerProfile.setValue(enlargerProfileEntity);
                        }

                        // Update the profile ID stored in the shared preferences, so that it'll
                        // still be selected when the user closes and reopens the app.
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                        SharedPreferences.Editor prefsEditor = prefs.edit();
                        prefsEditor.putInt(DEFAULT_PROFILE_ID_KEY, enlargerProfile.getValue().getId());
                        prefsEditor.commit();

                        recalculateLargerPrintExposureTime();
                    }
                });
            }
        });

        // Load the profile ID from the shared preferences, so that we can show the user the
        // enlarger they last had selected.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(application);
        int profileId = prefs.getInt(DEFAULT_PROFILE_ID_KEY, 0);
        state.set(ENLARGER_PROFILE_ID_KEY, profileId);
    }

    public void setSmallerPrintHeight(double smallerPrintHeight) {
        state.set(SMALLER_PRINT_HEIGHT_KEY, smallerPrintHeight);
        recalculateLargerPrintExposureTime();
    }

    public LiveData<Double> getSmallerPrintHeight() {
        return state.getLiveData(SMALLER_PRINT_HEIGHT_KEY, Double.NaN);
    }

    public void setSmallerPrintExposureTime(double smallerPrintExposureTime) {
        state.set(SMALLER_PRINT_EXPOSURE_TIME_KEY, smallerPrintExposureTime);
        recalculateLargerPrintExposureTime();
    }

    public LiveData<Double> getSmallerPrintExposureTime() {
        return state.getLiveData(SMALLER_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
    }

    public void setLargerPrintHeight(double largerPrintHeight) {
        state.set(LARGER_PRINT_HEIGHT_KEY, largerPrintHeight);
        recalculateLargerPrintExposureTime();
    }

    public LiveData<Double> getLargerPrintHeight() {
        return state.getLiveData(LARGER_PRINT_HEIGHT_KEY, Double.NaN);
    }

    public LiveData<Double> getLargerPrintExposureTime() {
        return state.getLiveData(LARGER_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
    }

    public void setEnlargerProfile(EnlargerProfile enlargerProfile) {
        state.set(ENLARGER_PROFILE_ID_KEY, enlargerProfile.getId());
    }

    public LiveData<EnlargerProfile> getEnlargerProfile() {
        return enlargerProfile;
    }

    public LiveData<EnlargerHeightErrorEvent> getSmallerPrintHeightError() {
        return smallerPrintHeightError;
    }

    public LiveData<EnlargerHeightErrorEvent> getLargerPrintHeightError() {
        return largerPrintHeightError;
    }

    public LiveData<Boolean> getEnlargerProfileValid() {
        return enlargerProfileValid;
    }

    public boolean isPrintDataValid() {
        final double smallerPrintHeight = getSmallerPrintHeight().getValue();
        final double smallerPrintExposureTime = getSmallerPrintExposureTime().getValue();
        final double largerPrintHeight = getLargerPrintHeight().getValue();
        final double offset = validEnlargerHeightOffset();

        return !Double.isNaN(smallerPrintHeight)
                && !Double.isNaN(smallerPrintExposureTime)
                && !Double.isNaN(largerPrintHeight)
                && (smallerPrintHeight + offset) > 0
                && smallerPrintExposureTime > 0
                && (largerPrintHeight + offset) > 0
                && largerPrintHeight > smallerPrintHeight;
    }

    public boolean isEnlargerProfileValid() {
        if (enlargerProfile.getValue() == null) {
            return false;
        }
        final EnlargerProfile profile = enlargerProfile.getValue();

        if (profile.getId() <= 0 || profile.getLensFocalLength() <= 0) {
            return false;
        }

        if (profile.hasTestExposures()) {
            final double offset = validEnlargerHeightOffset();

            return (profile.getSmallerTestDistance() + offset) > 0
                    && profile.getSmallerTestTime() > 0
                    && (profile.getLargerTestDistance() + offset) > 0
                    && profile.getLargerTestTime() > 0
                    && profile.getSmallerTestDistance() < profile.getLargerTestDistance()
                    && profile.getSmallerTestTime() < profile.getLargerTestTime()
                    && (profile.getSmallerTestDistance() + offset) >= PrintMath.computeMinimumHeight(profile.getLensFocalLength())
                    && (profile.getLargerTestDistance() + offset) >= PrintMath.computeMinimumHeight(profile.getLensFocalLength());
        } else {
            return true;
        }
    }

    private void validateSmallerPrintHeight() {
        final double smallerPrintHeight = getSmallerPrintHeight().getValue();

        if (isEnlargerProfileValid() && !Double.isNaN(smallerPrintHeight) && smallerPrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double smallerHeightValue = smallerPrintHeight + offset;
            if (smallerHeightValue <= 0) {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (smallerHeightValue < PrintMath.computeMinimumHeight(enlargerProfile.getValue().getLensFocalLength())) {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.TOO_LOW_FOR_FOCAL_LENGTH);
            } else {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
            }
        } else {
            smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
        }
    }

    private void validateLargerPrintHeight() {
        final double largerPrintHeight = getLargerPrintHeight().getValue();
        final double smallerPrintHeight = getSmallerPrintHeight().getValue();

        if (isEnlargerProfileValid() && !Double.isNaN(largerPrintHeight) && largerPrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double largerHeightValue = largerPrintHeight + offset;
            if (largerHeightValue <= 0
                    || (!Double.isNaN(smallerPrintHeight)
                    && smallerPrintHeight > 0
                    && smallerPrintHeight + offset >= largerHeightValue)) {
                largerPrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (largerHeightValue < PrintMath.computeMinimumHeight(enlargerProfile.getValue().getLensFocalLength())) {
                largerPrintHeightError.setValue(EnlargerHeightErrorEvent.TOO_LOW_FOR_FOCAL_LENGTH);
            } else {
                largerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
            }
        } else {
            largerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
        }
    }

    private double validEnlargerHeightOffset() {
        double offset;
        EnlargerProfile profile = enlargerProfile.getValue();
        if (profile != null) {
            offset = profile.getHeightMeasurementOffset();
            if (Double.isNaN(offset) || Double.isInfinite(offset)) {
                offset = 0.0d;
            }
        } else {
            offset = 0.0d;
        }
        return offset;
    }

    private void recalculateLargerPrintExposureTime() {
        validateSmallerPrintHeight();
        validateLargerPrintHeight();

        boolean enlargerValid = isEnlargerProfileValid();
        boolean printDataValid = isPrintDataValid();

        enlargerProfileValid.setValue(enlargerValid);

        if (!printDataValid || !enlargerValid) {
            state.set(LARGER_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        final double offset = validEnlargerHeightOffset();
        final double smallerHeightValue = getSmallerPrintHeight().getValue() + offset;
        final double smallerExposureValue = getSmallerPrintExposureTime().getValue();
        final double largerHeightValue = getLargerPrintHeight().getValue() + offset;


        Enlarger enlarger = Enlarger.createFromProfile(enlargerProfile.getValue());
        PrintScaler printScaler = new PrintScaler(enlarger);
        double largerExposureValue = printScaler.scalePrintTime(smallerHeightValue, smallerExposureValue, largerHeightValue);

        Log.d(TAG, "Smaller Print: " + smallerHeightValue + "mm" + ", " + smallerExposureValue + "s");
        Log.d(TAG, "Larger Print: " + largerHeightValue + "mm" + ", " + largerExposureValue + "s");

        state.set(LARGER_PRINT_EXPOSURE_TIME_KEY, largerExposureValue);
    }
}