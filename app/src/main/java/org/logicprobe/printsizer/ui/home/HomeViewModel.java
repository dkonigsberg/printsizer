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
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;
import org.logicprobe.printsizer.db.entity.PaperGradeEntity;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.Enlarger;
import org.logicprobe.printsizer.model.EnlargerProfile;
import org.logicprobe.printsizer.model.ExposureAdjustment;
import org.logicprobe.printsizer.model.PaperGrade;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.model.PrintMath;
import org.logicprobe.printsizer.model.PrintScaler;
import org.logicprobe.printsizer.ui.Converter;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final EnlargerProfile DEFAULT_PROFILE = new EnlargerProfileEntity();
    private static final PaperProfile DEFAULT_PAPER_PROFILE = new PaperProfileEntity();
    private static final int DEFAULT_PAPER_GRADE_ID = PaperProfile.GRADE_2;
    private static final String DEFAULT_PROFILE_ID_KEY = "default_enlarger_profile_id";
    private static final String BASE_PRINT_HEIGHT_KEY = "base_print_height";
    private static final String BASE_PRINT_EXPOSURE_TIME_KEY = "base_print_exposure_time";
    private static final String TARGET_PRINT_HEIGHT_KEY = "target_print_height";
    private static final String TARGET_PRINT_EXPOSURE_TIME_KEY = "target_print_exposure_time";
    private static final String TARGET_PRINT_EXPOSURE_OFFSET_KEY = "target_print_exposure_offset";
    private static final String BASE_PAPER_PROFILE_ID_KEY = "base_paper_profile_id";
    private static final String BASE_PAPER_GRADE_ID_KEY = "base_paper_grade_id";
    private static final String TARGET_PAPER_PROFILE_ID_KEY = "target_paper_profile_id";
    private static final String TARGET_PAPER_GRADE_ID_KEY = "target_paper_grade_id";
    private static final String PAPER_PROFILES_ADDED_KEY = "paper_profiles_added";
    private static final String ENLARGER_PROFILE_ID_KEY = "enlarger_profile_id";
    private static final String BURN_DODGE_ITEM_COUNTER_KEY = "burn_dodge_item_counter";
    private static final String BASE_BURN_DODGE_LIST_KEY = "base_burn_dodge_list";
    private static final String TARGET_BURN_DODGE_LIST_KEY = "target_burn_dodge_list";

    private final SavedStateHandle state;
    private final DataRepository repository;

    private LiveData<Boolean> paperProfilesAvailable;
    private MediatorLiveData<PaperProfile> basePaperProfile;
    private LiveData<PaperProfileEntity> loadedBasePaperProfile;
    private MediatorLiveData<Integer> basePaperGradeResourceId;
    private MediatorLiveData<PaperProfile> targetPaperProfile;
    private LiveData<PaperProfileEntity> loadedTargetPaperProfile;
    private MediatorLiveData<Integer> targetPaperGradeResourceId;
    private MediatorLiveData<EnlargerProfile> enlargerProfile;
    private LiveData<EnlargerProfileEntity> loadedEnlargerProfile;
    private MutableLiveData<EnlargerHeightErrorEvent> basePrintHeightError;
    private MutableLiveData<EnlargerHeightErrorEvent> targetPrintHeightError;
    private MutableLiveData<Boolean> enlargerProfileValid;

    public HomeViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        this.repository = ((App)application).getRepository();
        this.basePaperProfile = new MediatorLiveData<>();
        this.basePaperGradeResourceId = new MediatorLiveData<>();
        this.targetPaperProfile = new MediatorLiveData<>();
        this.targetPaperGradeResourceId = new MediatorLiveData<>();
        this.enlargerProfile = new MediatorLiveData<>();
        this.basePrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.targetPrintHeightError = new MutableLiveData<>(EnlargerHeightErrorEvent.NONE);
        this.enlargerProfileValid = new MutableLiveData<>(true);

        this.paperProfilesAvailable = Transformations.map(repository.numPaperProfiles(), new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer num) {
                return num != null && num > 0;
            }
        });

        // Set empty lists for the burn/dodge item lists
        state.set(BURN_DODGE_ITEM_COUNTER_KEY, 0L);
        List<BurnDodgeItem> emptyBaseList = new ArrayList<>();
        state.set(BASE_BURN_DODGE_LIST_KEY, emptyBaseList);
        List<BurnDodgeTargetItem> emptyTargetList = new ArrayList<>();
        state.set(TARGET_BURN_DODGE_LIST_KEY, emptyTargetList);

        // Set placeholder values indicating unset paper grades, so that our change listeners
        // will work correctly when they are set.
        state.set(BASE_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
        state.set(TARGET_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);

        basePaperProfile.setValue(DEFAULT_PAPER_PROFILE);
        basePaperProfile.addSource(state.getLiveData(BASE_PAPER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedBasePaperProfile != null) {
                    basePaperProfile.removeSource(loadedBasePaperProfile);
                    loadedBasePaperProfile = null;
                }
                if (profileId == 0) {
                    basePaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                    state.set(BASE_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
                    recalculateTargetPrintExposureTime();
                    return;
                }
                loadedBasePaperProfile = repository.loadPaperProfile(profileId);
                basePaperProfile.addSource(loadedBasePaperProfile, new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        if (paperProfileEntity == null) {
                            basePaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                        } else {
                            basePaperProfile.setValue(paperProfileEntity);
                        }
                        Integer gradeIdValue = state.get(BASE_PAPER_GRADE_ID_KEY);
                        if (gradeIdValue != null && gradeIdValue == Integer.MIN_VALUE) {
                            state.set(BASE_PAPER_GRADE_ID_KEY, findDefaultPaperGradeId(paperProfileEntity));
                        }
                        if (paperProfileEntity == null) {
                            setPaperProfilesAdded(false);
                        }
                        recalculateTargetPrintExposureTime();
                    }
                });
            }
        });

        basePaperGradeResourceId.setValue(R.string.empty);
        basePaperGradeResourceId.addSource(state.getLiveData(BASE_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE), new Observer<Integer>() {
            @Override
            public void onChanged(Integer gradeId) {
                basePaperGradeResourceId.setValue(Converter.paperGradeToResourceId(gradeId));
            }
        });
        
        targetPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
        targetPaperProfile.addSource(state.getLiveData(TARGET_PAPER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedTargetPaperProfile != null) {
                    targetPaperProfile.removeSource(loadedTargetPaperProfile);
                    loadedTargetPaperProfile = null;
                }
                if (profileId == 0) {
                    targetPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                    state.set(TARGET_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
                    recalculateTargetPrintExposureTime();
                    return;
                }
                loadedTargetPaperProfile = repository.loadPaperProfile(profileId);
                targetPaperProfile.addSource(loadedTargetPaperProfile, new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        if (paperProfileEntity == null) {
                            targetPaperProfile.setValue(DEFAULT_PAPER_PROFILE);
                        } else {
                            targetPaperProfile.setValue(paperProfileEntity);
                        }
                        Integer gradeIdValue = state.get(TARGET_PAPER_GRADE_ID_KEY);
                        if (gradeIdValue != null && gradeIdValue == Integer.MIN_VALUE) {
                            state.set(TARGET_PAPER_GRADE_ID_KEY, findDefaultPaperGradeId(paperProfileEntity));
                        }
                        if (paperProfileEntity == null) {
                            setPaperProfilesAdded(false);
                        }
                        recalculateTargetPrintExposureTime();
                    }
                });
            }
        });
        
        targetPaperGradeResourceId.setValue(R.string.empty);
        targetPaperGradeResourceId.addSource(state.getLiveData(TARGET_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE), new Observer<Integer>() {
            @Override
            public void onChanged(Integer gradeId) {
                targetPaperGradeResourceId.setValue(Converter.paperGradeToResourceId(gradeId));
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
                    recalculateTargetPrintExposureTime();
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

                        recalculateTargetPrintExposureTime();
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

    public LiveData<Boolean> getPaperProfilesAvailable() {
        return paperProfilesAvailable;
    }

    public void setBasePrintHeight(double basePrintHeight) {
        state.set(BASE_PRINT_HEIGHT_KEY, basePrintHeight);
        recalculateTargetPrintExposureTime();
    }

    public LiveData<Double> getBasePrintHeight() {
        return state.getLiveData(BASE_PRINT_HEIGHT_KEY, Double.NaN);
    }

    public void setBasePrintExposureTime(double basePrintExposureTime) {
        state.set(BASE_PRINT_EXPOSURE_TIME_KEY, basePrintExposureTime);
        recalculateTargetPrintExposureTime();
    }

    public LiveData<Double> getBasePrintExposureTime() {
        return state.getLiveData(BASE_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
    }

    public void setBasePaperProfileId(int paperProfileId) {
        state.set(BASE_PAPER_PROFILE_ID_KEY, paperProfileId);
    }

    public LiveData<PaperProfile> getBasePaperProfile() {
        return basePaperProfile;
    }

    public void setBasePaperGradeId(@PaperProfile.GradeId int paperGradeId) {
        state.set(BASE_PAPER_GRADE_ID_KEY, paperGradeId);
    }
    
    public LiveData<Integer> getBasePaperGradeId() {
        return state.getLiveData(BASE_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getBasePaperGradeResourceId() {
        return basePaperGradeResourceId;
    }

    public void setTargetPrintHeight(double targetPrintHeight) {
        state.set(TARGET_PRINT_HEIGHT_KEY, targetPrintHeight);
        recalculateTargetPrintExposureTime();
    }

    public LiveData<Double> getTargetPrintHeight() {
        return state.getLiveData(TARGET_PRINT_HEIGHT_KEY, Double.NaN);
    }

    public LiveData<Double> getTargetPrintExposureTime() {
        return state.getLiveData(TARGET_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
    }

    public void setTargetPrintExposureOffset(double exposureOffset) {
        state.set(TARGET_PRINT_EXPOSURE_OFFSET_KEY, exposureOffset);
        recalculateTargetPrintExposureTime();
    }

    public LiveData<Double> getTargetPrintExposureOffset() {
        return state.getLiveData(TARGET_PRINT_EXPOSURE_OFFSET_KEY, 0.0d);
    }

    public void setTargetPaperProfileId(int paperProfileId) {
        state.set(TARGET_PAPER_PROFILE_ID_KEY, paperProfileId);
    }

    public LiveData<PaperProfile> getTargetPaperProfile() {
        return targetPaperProfile;
    }
    
    public void setTargetPaperGradeId(@PaperProfile.GradeId int paperGradeId) {
        state.set(TARGET_PAPER_GRADE_ID_KEY, paperGradeId);
    }

    public LiveData<Integer> getTargetPaperGradeId() {
        return state.getLiveData(TARGET_PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getTargetPaperGradeResourceId() {
        return targetPaperGradeResourceId;
    }

    public void setPaperProfilesAdded(boolean paperProfilesAdded) {
        state.set(PAPER_PROFILES_ADDED_KEY, paperProfilesAdded);

        // If we're setting paper profiles as being disabled, clear any existing
        // values. This ensures that we start from a clean slate if profiles are
        // added again.
        if (!paperProfilesAdded) {
            state.set(BASE_PAPER_PROFILE_ID_KEY, 0);
            state.set(BASE_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
            state.set(TARGET_PAPER_PROFILE_ID_KEY, 0);
            state.set(TARGET_PAPER_GRADE_ID_KEY, Integer.MIN_VALUE);
        }
    }

    public LiveData<Boolean> getPaperProfilesAdded() {
        return state.getLiveData(PAPER_PROFILES_ADDED_KEY, false);
    }

    public LiveData<List<BurnDodgeItem>> getBaseBurnDodgeList() {
        return state.getLiveData(BASE_BURN_DODGE_LIST_KEY);
    }

    public void addBaseBurnDodgeItem(BurnDodgeItem item) {
        // Ensure that the newly added item has a unique ID
        long counter = Util.safeGetStateLong(state, BURN_DODGE_ITEM_COUNTER_KEY, 0L);
        item.setItemId(counter++);
        state.set(BURN_DODGE_ITEM_COUNTER_KEY, counter);

        List<BurnDodgeItem> itemList = state.get(BASE_BURN_DODGE_LIST_KEY);
        if (itemList == null) {
            itemList = new ArrayList<>();
        } else {
            itemList = new ArrayList<>(itemList);
        }
        itemList.add(item);
        reindexBurnDodgeItemList(itemList);
        state.set(BASE_BURN_DODGE_LIST_KEY, itemList);
        recalculateTargetBurnDodgeTimes();
    }

    public boolean updateBaseBurnDodgeItem(BurnDodgeItem item) {
        if (item == null || item.getItemId() < 0) {
            return false;
        }

        List<BurnDodgeItem> itemList = state.get(BASE_BURN_DODGE_LIST_KEY);
        if (itemList == null || itemList.size() == 0) {
            return false;
        }

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getItemId() == item.getItemId()) {
                itemList = new ArrayList<>(itemList);
                itemList.set(i, new BurnDodgeItem(item));
                state.set(BASE_BURN_DODGE_LIST_KEY, itemList);
                recalculateTargetBurnDodgeTimes();
                return true;
            }
        }
        return false;
    }

    public void removeBaseBurnDodgeItem(BurnDodgeItem item) {
        List<BurnDodgeItem> itemList = state.get(BASE_BURN_DODGE_LIST_KEY);
        if (itemList != null) {
            itemList = new ArrayList<>(itemList);
            if (itemList.remove(item)) {
                reindexBurnDodgeItemList(itemList);
                state.set(BASE_BURN_DODGE_LIST_KEY, itemList);
                recalculateTargetBurnDodgeTimes();
            }
        }
    }

    private void reindexBurnDodgeItemList(List<BurnDodgeItem> itemList) {
        int index = 0;
        for (BurnDodgeItem item : itemList) {
            item.setIndex(index++);
        }
    }

    public LiveData<List<BurnDodgeTargetItem>> getTargetBurnDodgeList() {
        return state.getLiveData(TARGET_BURN_DODGE_LIST_KEY);
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

    public LiveData<EnlargerHeightErrorEvent> getBasePrintHeightError() {
        return basePrintHeightError;
    }

    public LiveData<EnlargerHeightErrorEvent> getTargetPrintHeightError() {
        return targetPrintHeightError;
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
        final double basePrintHeight = LiveDataUtil.getDoubleValue(getBasePrintHeight());
        final double basePrintExposureTime = LiveDataUtil.getDoubleValue(getBasePrintExposureTime());
        final double targetPrintHeight = LiveDataUtil.getDoubleValue(getTargetPrintHeight());
        final double offset = validEnlargerHeightOffset();

        return !Double.isNaN(basePrintHeight)
                && !Double.isNaN(basePrintExposureTime)
                && !Double.isNaN(targetPrintHeight)
                && (basePrintHeight + offset) > 0
                && basePrintExposureTime > 0
                && (targetPrintHeight + offset) > 0;
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

    private void validateBasePrintHeight() {
        final double basePrintHeight = LiveDataUtil.getDoubleValue(getBasePrintHeight());

        if (isEnlargerProfileValid() && !Double.isNaN(basePrintHeight) && basePrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double baseHeightValue = basePrintHeight + offset;
            final EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
            if (baseHeightValue <= 0) {
                basePrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (enlargerProfileValue != null && baseHeightValue < PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength())) {
                basePrintHeightError.setValue(EnlargerHeightErrorEvent.TOO_LOW_FOR_FOCAL_LENGTH);
            } else {
                basePrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
            }
        } else {
            basePrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
        }
    }

    private void validateTargetPrintHeight() {
        final double targetPrintHeight = LiveDataUtil.getDoubleValue(getTargetPrintHeight());

        if (isEnlargerProfileValid() && !Double.isNaN(targetPrintHeight) && targetPrintHeight > 0) {
            final double offset = validEnlargerHeightOffset();
            final double targetHeightValue = targetPrintHeight + offset;
            final EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
            if (targetHeightValue <= 0) {
                targetPrintHeightError.setValue(EnlargerHeightErrorEvent.INVALID);
            } else if (enlargerProfileValue != null && targetHeightValue < PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength())) {
                targetPrintHeightError.setValue(EnlargerHeightErrorEvent.TOO_LOW_FOR_FOCAL_LENGTH);
            } else {
                targetPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
            }
        } else {
            targetPrintHeightError.setValue(EnlargerHeightErrorEvent.NONE);
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

    private void recalculateTargetPrintExposureTime() {
        validateBasePrintHeight();
        validateTargetPrintHeight();

        boolean enlargerValid = isEnlargerProfileValid();
        boolean printDataValid = isPrintDataValid();

        enlargerProfileValid.setValue(enlargerValid);

        if (!printDataValid || !enlargerValid) {
            state.set(TARGET_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        EnlargerProfile enlargerProfileValue = enlargerProfile.getValue();
        if (enlargerProfileValue == null) {
            state.set(TARGET_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        final double offset = validEnlargerHeightOffset();
        final double baseHeightValue = LiveDataUtil.getDoubleValue(getBasePrintHeight()) + offset;
        final double baseExposureValue = LiveDataUtil.getDoubleValue(getBasePrintExposureTime());
        final double targetHeightValue = LiveDataUtil.getDoubleValue(getTargetPrintHeight()) + offset;
        final double targetExposureOffset = LiveDataUtil.getDoubleValue(getTargetPrintExposureOffset());
        final double minHeight = PrintMath.computeMinimumHeight(enlargerProfileValue.getLensFocalLength());

        if (baseHeightValue < minHeight || targetHeightValue < minHeight) {
            state.set(TARGET_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
            return;
        }

        // Pluck out the ISO(P) values for the two paper configurations, if they are available.
        // (This should probably be replaced with something more elegant later.)
        int baseIsoP = 0;
        int targetIsoP = 0;
        if (LiveDataUtil.getBooleanValue(getPaperProfilesAdded())) {
            PaperGrade baseGrade = getSelectedGrade(
                    basePaperProfile.getValue(), LiveDataUtil.getIntValue(getBasePaperGradeId()));
            PaperGrade targetGrade = getSelectedGrade(
                    targetPaperProfile.getValue(), LiveDataUtil.getIntValue(getTargetPaperGradeId()));
            if (baseGrade.getIsoP() > 0 && targetGrade.getIsoP() > 0) {
                baseIsoP = baseGrade.getIsoP();
                targetIsoP = targetGrade.getIsoP();
            }
        }

        Enlarger enlarger = Enlarger.createFromProfile(enlargerProfileValue);
        PrintScaler printScaler = new PrintScaler(enlarger);

        printScaler.setBaseHeight(baseHeightValue);
        printScaler.setBaseExposureTime(baseExposureValue);
        printScaler.setBaseIsoP(baseIsoP);

        printScaler.setTargetHeight(targetHeightValue);
        printScaler.setTargetIsoP(targetIsoP);

        printScaler.setExposureCompensation(targetExposureOffset);

        double targetExposureValue = printScaler.calculateTargetExposureTime();

        state.set(TARGET_PRINT_EXPOSURE_TIME_KEY, targetExposureValue);
        recalculateTargetBurnDodgeTimes();
    }

    private void recalculateTargetBurnDodgeTimes() {
        double baseExposureTime = Util.safeGetStateDouble(state, BASE_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
        double targetExposureTime = Util.safeGetStateDouble(state, TARGET_PRINT_EXPOSURE_TIME_KEY, Double.NaN);
        List<BurnDodgeItem> itemList = state.get(BASE_BURN_DODGE_LIST_KEY);
        if (itemList != null && itemList.size() > 0
                && Util.isValidPositive(baseExposureTime)
                && Util.isValidPositive(targetExposureTime)) {
            List<BurnDodgeTargetItem> targetItemList = new ArrayList<>(itemList.size());
            for (BurnDodgeItem item : itemList) {
                BurnDodgeTargetItem targetItem = new BurnDodgeTargetItem();
                targetItem.setItemId(item.getItemId());
                targetItem.setIndex(item.getIndex());
                targetItem.setName(item.getName());
                double burnDodgeTime = calculateBurnDodgeTime(baseExposureTime, targetExposureTime, item.getAdjustment());
                if ((targetExposureTime + burnDodgeTime) < 0.1d) {
                    burnDodgeTime = targetExposureTime * -1.0d;
                }
                targetItem.setSecondsValue(burnDodgeTime);
                targetItemList.add(targetItem);
            }
            state.set(TARGET_BURN_DODGE_LIST_KEY, targetItemList);
        } else {
            List<BurnDodgeTargetItem> targetItemList = state.get(TARGET_BURN_DODGE_LIST_KEY);
            if (targetItemList != null && targetItemList.size() > 0) {
                targetItemList.clear();
                state.set(TARGET_BURN_DODGE_LIST_KEY, targetItemList);
            }
        }
    }

    private double calculateBurnDodgeTime(double baseExposureTime, double targetExposureTime, ExposureAdjustment adjustment) {
        if (adjustment.getUnit() == ExposureAdjustment.UNIT_SECONDS) {
            if (baseExposureTime + adjustment.getSecondsValue() < 0.1d) {
                return targetExposureTime * -1.0d;
            } else {
                double stopsDiff = PrintMath.timeDifferenceInStops(baseExposureTime, baseExposureTime + adjustment.getSecondsValue());
                return PrintMath.timeAdjustInStops(targetExposureTime, stopsDiff) - targetExposureTime;
            }
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_PERCENT) {
            return (adjustment.getPercentValue() / 100d) * targetExposureTime;
        } else if (adjustment.getUnit() == ExposureAdjustment.UNIT_STOPS) {
            return PrintMath.timeAdjustInStops(targetExposureTime, adjustment.getStopsValue().doubleValue()) - targetExposureTime;
        } else {
            return 0d;
        }
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