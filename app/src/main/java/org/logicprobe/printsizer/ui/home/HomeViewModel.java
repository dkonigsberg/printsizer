package org.logicprobe.printsizer.ui.home;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.preference.PreferenceManager;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.LiveDataUtil;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.db.entity.PaperGradeEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.Enlarger;
import org.logicprobe.printsizer.model.EnlargerProfile;
import org.logicprobe.printsizer.model.PaperGrade;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.model.PrintMath;
import org.logicprobe.printsizer.model.PrintScaler;
import org.logicprobe.printsizer.ui.Converter;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final EnlargerProfile DEFAULT_PROFILE = new EnlargerProfileEntity();
    private static final PaperProfile DEFAULT_PAPER_PROFILE = new PaperProfileEntity();
    private static final int DEFAULT_PAPER_GRADE_ID = PaperProfile.GRADE_2;
    private static final String DEFAULT_PROFILE_ID_KEY = "default_enlarger_profile_id";
    private static final String SMALLER_PRINT_HEIGHT_KEY = "smaller_print_height";
    private static final String SMALLER_PRINT_EXPOSURE_TIME_KEY = "smaller_print_exposure_time";
    private static final String LARGER_PRINT_HEIGHT_KEY = "larger_print_height";
    private static final String LARGER_PRINT_EXPOSURE_TIME_KEY = "larger_print_exposure_time";
    private static final String LARGER_PRINT_EXPOSURE_OFFSET_KEY = "larger_print_exposure_offset";
    private static final String SMALLER_PAPER_PROFILE_ID_KEY = "smaller_paper_profile_id";
    private static final String SMALLER_PAPER_GRADE_ID_KEY = "smaller_paper_grade_id";
    private static final String LARGER_PAPER_PROFILE_ID_KEY = "larger_paper_profile_id";
    private static final String LARGER_PAPER_GRADE_ID_KEY = "larger_paper_grade_id";
    private static final String HAS_PAPER_PROFILES_KEY = "has_paper_profiles";
    private static final String ENLARGER_PROFILE_ID_KEY = "enlarger_profile_id";

    private final SavedStateHandle state;
    private final DataRepository repository;

    private LiveData<Boolean> hasPaperProfiles;
    private MediatorLiveData<PaperProfile> smallerPaperProfile;
    private LiveData<PaperProfileEntity> loadedSmallerPaperProfile;
    private MediatorLiveData<Integer> smallerPaperGradeResourceId;
    private MediatorLiveData<PaperProfile> largerPaperProfile;
    private LiveData<PaperProfileEntity> loadedLargerPaperProfile;
    private MediatorLiveData<Integer> largerPaperGradeResourceId;
    private MediatorLiveData<EnlargerProfile> enlargerProfile;
    private LiveData<EnlargerProfileEntity> loadedEnlargerProfile;
    private MutableLiveData<EnlargerHeightErrorEvent> smallerPrintHeightError;
    private MutableLiveData<EnlargerHeightErrorEvent> largerPrintHeightError;
    private MutableLiveData<Boolean> enlargerProfileValid;

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        this.repository = ((App)application).getRepository();
        this.smallerPaperProfile = new MediatorLiveData<>();
        this.smallerPaperGradeResourceId = new MediatorLiveData<>();
        this.largerPaperProfile = new MediatorLiveData<>();
        this.largerPaperGradeResourceId = new MediatorLiveData<>();
        this.enlargerProfile = new MediatorLiveData<>();
        this.smallerPrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.largerPrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.enlargerProfileValid = new MutableLiveData<>(true);

        this.hasPaperProfiles = Transformations.map(repository.numPaperProfiles(), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer num) {
                return num != null && num > 0;
            }
        });

        // Set placeholder values indicating unset paper grades, so that our change listeners
        // will work correctly when they are set.
        state.set(SMALLER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
        state.set(LARGER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);

        smallerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
        smallerPaperProfile.addSource(state.getLiveData(SMALLER_PAPER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedSmallerPaperProfile != null) {
                    smallerPaperProfile.removeSource(loadedSmallerPaperProfile);
                    loadedSmallerPaperProfile = null;
                }
                if (profileId == 0) {
                    smallerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                    state.set(SMALLER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
                    recalculateLargerPrintExposureTime();
                    return;
                }
                loadedSmallerPaperProfile = repository.loadPaperProfile(profileId);
                smallerPaperProfile.addSource(loadedSmallerPaperProfile, new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        if (paperProfileEntity == null) {
                            smallerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                        } else {
                            smallerPaperProfile.setValue(paperProfileEntity);
                        }
                        Integer gradeIdValue = state.get(SMALLER_PAPER_GRADE_ID_KEY);
                        if (gradeIdValue != null && gradeIdValue == Integer.MIN_VALUE) {
                            state.set(SMALLER_PAPER_GRADE_ID_KEY, findDefaultPaperGradeId(paperProfileEntity));
                        }
                        if (paperProfileEntity == null) {
                            setHasPaperProfiles(false);
                        }
                        recalculateLargerPrintExposureTime();
                    }
                });
            }
        });

        smallerPaperGradeResourceId.setValue(R.string.empty);
        smallerPaperGradeResourceId.addSource(state.getLiveData(SMALLER_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE), new Observer<Integer>() {
            @Override
            public void onChanged(Integer gradeId) {
                smallerPaperGradeResourceId.setValue(Converter.paperGradeToResourceId(gradeId));
            }
        });
        
        largerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
        largerPaperProfile.addSource(state.getLiveData(LARGER_PAPER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedLargerPaperProfile != null) {
                    largerPaperProfile.removeSource(loadedLargerPaperProfile);
                    loadedLargerPaperProfile = null;
                }
                if (profileId == 0) {
                    largerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                    state.set(LARGER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
                    recalculateLargerPrintExposureTime();
                    return;
                }
                loadedLargerPaperProfile = repository.loadPaperProfile(profileId);
                largerPaperProfile.addSource(loadedLargerPaperProfile, new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        if (paperProfileEntity == null) {
                            largerPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                        } else {
                            largerPaperProfile.setValue(paperProfileEntity);
                        }
                        Integer gradeIdValue = state.get(LARGER_PAPER_GRADE_ID_KEY);
                        if (gradeIdValue != null && gradeIdValue == Integer.MIN_VALUE) {
                            state.set(LARGER_PAPER_GRADE_ID_KEY, findDefaultPaperGradeId(paperProfileEntity));
                        }
                        if (paperProfileEntity == null) {
                            setHasPaperProfiles(false);
                        }
                        recalculateLargerPrintExposureTime();
                    }
                });
            }
        });
        
        largerPaperGradeResourceId.setValue(R.string.empty);
        largerPaperGradeResourceId.addSource(state.getLiveData(LARGER_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE), new Observer<Integer>() {
            @Override
            public void onChanged(Integer gradeId) {
                largerPaperGradeResourceId.setValue(Converter.paperGradeToResourceId(gradeId));
            }
        });

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
                        int profileId;
                        if (enlargerProfileEntity == null) {
                            enlargerProfile.setValue(DEFAULT_PROFILE);
                            profileId = 0;
                        } else {
                            enlargerProfile.setValue(enlargerProfileEntity);
                            profileId = enlargerProfileEntity.getId();
                        }

                        // Update the profile ID stored in the shared preferences, so that it'll
                        // still be selected when the user closes and reopens the app.
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                        SharedPreferences.Editor prefsEditor = prefs.edit();
                        prefsEditor.putInt(DEFAULT_PROFILE_ID_KEY, profileId);
                        prefsEditor.apply();

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

    public LiveData<Boolean> getHasPaperProfiles() {
        return hasPaperProfiles;
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

    public void setSmallerPaperProfileId(int paperProfileId) {
        state.set(SMALLER_PAPER_PROFILE_ID_KEY, paperProfileId);
    }

    public LiveData<PaperProfile> getSmallerPaperProfile() {
        return smallerPaperProfile;
    }

    public void setSmallerPaperGradeId(@PaperProfile.GradeId int paperGradeId) {
        state.set(SMALLER_PAPER_GRADE_ID_KEY, paperGradeId);
    }
    
    public LiveData<Integer> getSmallerPaperGradeId() {
        return state.getLiveData(SMALLER_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getSmallerPaperGradeResourceId() {
        return smallerPaperGradeResourceId;
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

    public void setLargerPrintExposureOffset(double exposureOffset) {
        state.set(LARGER_PRINT_EXPOSURE_OFFSET_KEY, exposureOffset);
        recalculateLargerPrintExposureTime();
    }

    public LiveData<Double> getLargerPrintExposureOffset() {
        return state.getLiveData(LARGER_PRINT_EXPOSURE_OFFSET_KEY, 0.0d);
    }

    public void setLargerPaperProfileId(int paperProfileId) {
        state.set(LARGER_PAPER_PROFILE_ID_KEY, paperProfileId);
    }

    public LiveData<PaperProfile> getLargerPaperProfile() {
        return largerPaperProfile;
    }
    
    public void setLargerPaperGradeId(@PaperProfile.GradeId int paperGradeId) {
        state.set(LARGER_PAPER_GRADE_ID_KEY, paperGradeId);
    }

    public LiveData<Integer> getLargerPaperGradeId() {
        return state.getLiveData(LARGER_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getLargerPaperGradeResourceId() {
        return largerPaperGradeResourceId;
    }

    public void setHasPaperProfiles(boolean hasPaperProfiles) {
        state.set(HAS_PAPER_PROFILES_KEY, hasPaperProfiles);

        // If we're setting paper profiles as being disabled, clear any existing
        // values. This ensures that we start from a clean slate if profiles are
        // added again.
        if (!hasPaperProfiles) {
            state.set(SMALLER_PAPER_PROFILE_ID_KEY, 0);
            state.set(SMALLER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
            state.set(LARGER_PAPER_PROFILE_ID_KEY, 0);
            state.set(LARGER_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
        }
    }

    public LiveData<Boolean> hasPaperProfiles() {
        return state.getLiveData(HAS_PAPER_PROFILES_KEY, false);
    }

    public void setEnlargerProfile(int enlargerProfileId) {
        state.set(ENLARGER_PROFILE_ID_KEY, enlargerProfileId);
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


    private static int findDefaultPaperGradeId(PaperProfile paperProfile) {
        // Attempt to find a starting contrast grade setting, based on what is available in the
        // provided paper profile. This gives a preference to grades 2 and 3, followed by the
        // unfiltered setting, and eventually whatever we have.
        if (paperProfile == null) {
            return DEFAULT_PAPER_GRADE_ID;
        } else if (paperProfile.getGrade2() != null && paperProfile.getGrade2().getIsoP() > 0) {
            return PaperProfile.GRADE_2;
        } else if (paperProfile.getGrade3() != null && paperProfile.getGrade3().getIsoP() > 0) {
            return PaperProfile.GRADE_3;
        } else if (paperProfile.getGradeNone() != null && paperProfile.getGradeNone().getIsoP() > 0) {
            return PaperProfile.GRADE_NONE;
        } else if (paperProfile.getGrade0() != null && paperProfile.getGrade0().getIsoP() > 0) {
            return PaperProfile.GRADE_0;
        } else if (paperProfile.getGrade00() != null && paperProfile.getGrade00().getIsoP() > 0) {
            return PaperProfile.GRADE_00;
        } else if (paperProfile.getGrade4() != null && paperProfile.getGrade4().getIsoP() > 0) {
            return PaperProfile.GRADE_4;
        } else if (paperProfile.getGrade5() != null && paperProfile.getGrade5().getIsoP() > 0) {
            return PaperProfile.GRADE_5;
        } else {
            return PaperProfile.GRADE_NONE;
        }
    }

    public boolean isPrintDataValid() {
        final double smallerPrintHeight = LiveDataUtil.getDoubleValue(getSmallerPrintHeight());
        final double smallerPrintExposureTime = LiveDataUtil.getDoubleValue(getSmallerPrintExposureTime());
        final double largerPrintHeight = LiveDataUtil.getDoubleValue(getLargerPrintHeight());
        final double offset = validEnlargerHeightOffset();

        return !Double.isNaN(smallerPrintHeight)
                && !Double.isNaN(smallerPrintExposureTime)
                && !Double.isNaN(largerPrintHeight)
                && (smallerPrintHeight + offset) > 0
                && smallerPrintExposureTime > 0
                && (largerPrintHeight + offset) > 0
                && largerPrintHeight >= smallerPrintHeight;
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
        final double smallerPrintHeight = LiveDataUtil.getDoubleValue(getSmallerPrintHeight());

        if (isEnlargerProfileValid() && !Double.isNaN(smallerPrintHeight) && smallerPrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double smallerHeightValue = smallerPrintHeight + offset;
            final EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
            if (smallerHeightValue <= 0) {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (enlargerProfileValue != null && smallerHeightValue < PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength())) {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.TOO_LOW_FOR_FOCAL_LENGTH);
            } else {
                smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
            }
        } else {
            smallerPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
        }
    }

    private void validateLargerPrintHeight() {
        final double largerPrintHeight = LiveDataUtil.getDoubleValue(getLargerPrintHeight());
        final double smallerPrintHeight = LiveDataUtil.getDoubleValue(getSmallerPrintHeight());

        if (isEnlargerProfileValid() && !Double.isNaN(largerPrintHeight) && largerPrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double largerHeightValue = largerPrintHeight + offset;
            final EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
            if (largerHeightValue <= 0
                    || (!Double.isNaN(smallerPrintHeight)
                    && smallerPrintHeight > 0
                    && smallerPrintHeight + offset > largerHeightValue)) {
                largerPrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (enlargerProfileValue != null && largerHeightValue < PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength())) {
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

        EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
        if (enlargerProfileValue == null) {
            state.set(LARGER_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        final double offset = validEnlargerHeightOffset();
        final double smallerHeightValue = LiveDataUtil.getDoubleValue(getSmallerPrintHeight()) + offset;
        final double smallerExposureValue = LiveDataUtil.getDoubleValue(getSmallerPrintExposureTime());
        final double largerHeightValue = LiveDataUtil.getDoubleValue(getLargerPrintHeight()) + offset;
        final double largerExposureOffset = LiveDataUtil.getDoubleValue(getLargerPrintExposureOffset());

        if (smallerHeightValue < PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength())) {
            state.set(LARGER_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        // Pluck out the ISO(P) values for the two paper configurations, if they are available.
        // (This should probably be replaced with something more elegant later.)
        int smallerIsoP = 0;
        int largerIsoP = 0;
        if (LiveDataUtil.getBooleanValue(hasPaperProfiles())) {
            PaperGrade smallerGrade = getSelectedGrade(
                    smallerPaperProfile.getValue(), LiveDataUtil.getIntValue(getSmallerPaperGradeId()));
            PaperGrade largerGrade = getSelectedGrade(
                    largerPaperProfile.getValue(), LiveDataUtil.getIntValue(getLargerPaperGradeId()));
            if (smallerGrade.getIsoP() > 0 && largerGrade.getIsoP() > 0) {
                smallerIsoP = smallerGrade.getIsoP();
                largerIsoP = largerGrade.getIsoP();
            }
        }

        Enlarger enlarger = Enlarger.createFromProfile(enlargerProfileValue);
        PrintScaler printScaler = new PrintScaler(enlarger);

        printScaler.setBaseHeight(smallerHeightValue);
        printScaler.setBaseExposureTime(smallerExposureValue);
        printScaler.setBaseIsoP(smallerIsoP);

        printScaler.setTargetHeight(largerHeightValue);
        printScaler.setTargetIsoP(largerIsoP);

        printScaler.setExposureCompensation(largerExposureOffset);

        double largerExposureValue = printScaler.calculateTargetExposureTime();

        state.set(LARGER_PRINT_EXPOSURE_TIME_KEY, largerExposureValue);
    }

    private PaperGrade getSelectedGrade(@Nullable PaperProfile paperProfile, int gradeId) {
        PaperGrade grade = null;
        if (paperProfile != null) {
            grade = paperProfile.getGrade(gradeId);
        }
        if (grade == null) {
            grade = new PaperGradeEntity();
        }
        return grade;
    }
}