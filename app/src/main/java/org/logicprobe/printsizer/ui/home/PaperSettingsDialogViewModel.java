package org.logicprobe.printsizer.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.Util;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperGrade;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.Converter;

import java.util.ArrayList;
import java.util.List;

public class PaperSettingsDialogViewModel extends AndroidViewModel {
    private static final String TAG = PaperSettingsDialogViewModel.class.getSimpleName();
    private static final PaperProfileEntity DEFAULT_PAPER_PROFILE = new PaperProfileEntity();
    private static final String INITIALIZED_KEY = "initialized";
    private static final String PAPER_PROFILE_KEY = "paper_profile";
    private static final String PAPER_GRADE_ID_KEY = "paper_grade_id";
    private static final String REFERENCE_ISOR_KEY = "reference_isor";

    private static final int GRADE_UNSET = -10;

    private final SavedStateHandle state;

    private MutableLiveData<Integer> numGrades;
    private MutableLiveData<Integer> gradeIndex;
    private MutableLiveData<Integer> gradeLabelResourceId;
    private List<Integer> gradeIdList;

    public PaperSettingsDialogViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        this.numGrades = new MutableLiveData<>();
        this.gradeIndex = new MutableLiveData<>();
        this.gradeLabelResourceId = new MutableLiveData<>(R.string.empty);
        this.gradeIdList = new ArrayList<>();
    }

    public void setInitialized(boolean initialized) {
        boolean wasInitialized = Util.safeGetStateBoolean(state, INITIALIZED_KEY);
        state.set(INITIALIZED_KEY, initialized);

        if (!wasInitialized && initialized) {
            handleInitialState();
        }
    }

    public boolean isInitialized() {
        return Util.safeGetStateBoolean(state, INITIALIZED_KEY);
    }

    public void setPaperProfile(PaperProfileEntity paperProfile) {
        PaperProfileEntity previousProfile = state.get(PAPER_PROFILE_KEY);

        state.set(PAPER_PROFILE_KEY, paperProfile);

        if (isInitialized() && paperProfile != null && !paperProfile.equals(previousProfile)) {
            handlePaperProfileChanged(paperProfile);
        }
    }

    public LiveData<PaperProfileEntity> getPaperProfile() {
        return state.getLiveData(PAPER_PROFILE_KEY, DEFAULT_PAPER_PROFILE);
    }

    public void setPaperGrade(@PaperProfile.GradeId int gradeId) {
        state.set(PAPER_GRADE_ID_KEY, gradeId);

        if (isInitialized()) {
            handlePaperGradeChanged(gradeId);
        }
    }

    public LiveData<Integer> getPaperGrade() {
        return state.getLiveData(PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getNumGrades() {
        return numGrades;
    }

    public void setGradeIndex(int index) {
        if (index < 0 || index >= gradeIdList.size()) {
            setPaperGrade(PaperProfile.GRADE_NONE);
        } else {
            setPaperGrade(gradeIdList.get(index));
        }
    }

    public LiveData<Integer> getGradeIndex() {
        return gradeIndex;
    }

    public LiveData<Integer> getGradeLabelResourceId() {
        return gradeLabelResourceId;
    }

    public void setReferenceIsoR(int isoR) {
        state.set(REFERENCE_ISOR_KEY, isoR);
    }

    private void handleInitialState() {
        Log.d(TAG, "Paper settings updating for initial state");

        PaperProfileEntity paperProfile = state.get(PAPER_PROFILE_KEY);
        if (paperProfile == null) {
            Log.d(TAG, "No initial paper profile!");
            return;
        }

        int gradeId = Util.safeGetStateInt(state, PAPER_GRADE_ID_KEY, GRADE_UNSET);

        // The initial state includes setting up to the following properties:
        // - Paper Profile
        // - Paper Grade Id
        // - Reference ISO(R)

        updatePaperGradeIdList(paperProfile);

        if (gradeId == GRADE_UNSET) {
            gradeId = findDefaultGradeId();
            state.set(PAPER_GRADE_ID_KEY, gradeId);
        }
        updatePaperGradeIdDependents(gradeId);

        // Note: The reference ISO(R) is not typically used unless the paper profile is
        // explicitly changed after initialization.
    }

    private void handlePaperProfileChanged(PaperProfileEntity paperProfile) {
        Log.d(TAG, "Paper settings updating for paper profile change");
        if (paperProfile == null) {
            Log.d(TAG, "No new paper profile!");
            return;
        }

        updatePaperGradeIdList(paperProfile);

        int gradeId = GRADE_UNSET;
        int referenceIsoR = Util.safeGetStateInt(state, REFERENCE_ISOR_KEY, 0);
        if (referenceIsoR > 0) {
            gradeId = findMatchingGradeId(paperProfile, referenceIsoR);
        }

        if (gradeId == GRADE_UNSET) {
            // No matching grade was found, attempt to see if the newly selected paper
            // contains the previously selected grade.
            int previousGradeId = Util.safeGetStateInt(state, PAPER_GRADE_ID_KEY, GRADE_UNSET);
            if (previousGradeId != GRADE_UNSET) {
                PaperGrade paperGrade = paperProfile.getGrade(previousGradeId);
                if (paperGrade != null && paperGrade.getIsoP() > 0) {
                    Log.d(TAG, "Falling back to previously selected paper grade");
                    gradeId = previousGradeId;
                }
            }

            // Could not set the grade from the previous paper, so fall back to the default
            // grade selection behavior.
            if (gradeId == GRADE_UNSET) {
                Log.d(TAG, "Falling back to default grade selection behavior");
                gradeId = findDefaultGradeId();
            }
        }

        state.set(PAPER_GRADE_ID_KEY, gradeId);
        updatePaperGradeIdDependents(gradeId);
    }

    private void handlePaperGradeChanged(int gradeId) {
        Log.d(TAG, "Paper settings updating for paper grade change");
        updatePaperGradeIdDependents(gradeId);
    }

    private void updatePaperGradeIdList(PaperProfileEntity paperProfile) {
        gradeIdList.clear();
        if (paperProfile.getGradeNone() != null && paperProfile.getGradeNone().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_NONE);
        }
        if (paperProfile.getGrade00() != null && paperProfile.getGrade00().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_00);
        }
        if (paperProfile.getGrade0() != null && paperProfile.getGrade0().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_0);
        }
        if (paperProfile.getGrade1() != null && paperProfile.getGrade1().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_1);
        }
        if (paperProfile.getGrade2() != null && paperProfile.getGrade2().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_2);
        }
        if (paperProfile.getGrade3() != null && paperProfile.getGrade3().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_3);
        }
        if (paperProfile.getGrade4() != null && paperProfile.getGrade4().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_4);
        }
        if (paperProfile.getGrade5() != null && paperProfile.getGrade5().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_5);
        }
        numGrades.setValue(gradeIdList.size() - 1);
    }

    private void updatePaperGradeIdDependents(int gradeId) {
        int index = findGradeIndex(gradeId);
        if (index >= 0) {
            int resourceId = Converter.paperGradeToResourceId(gradeId);
            gradeIndex.setValue(index);
            gradeLabelResourceId.setValue(resourceId);
        }
    }

    private int findMatchingGradeId(PaperProfile paperProfile, int referenceIsoR) {
        final int[] gradeList = new int[]{
                PaperProfile.GRADE_00, PaperProfile.GRADE_0, PaperProfile.GRADE_1,
                PaperProfile.GRADE_2, PaperProfile.GRADE_3, PaperProfile.GRADE_4,
                PaperProfile.GRADE_5};

        Log.d(TAG, "Using reference ISO(R) of " + referenceIsoR + " to find a matching grade");

        int matchingGradeId = GRADE_UNSET;
        int matchingGradeDiff = 0;
        for (int gradeId : gradeList) {
            PaperGrade grade = paperProfile.getGrade(gradeId);
            if (grade != null && grade.getIsoP() > 0 && grade.getIsoR() > 0) {
                int gradeDiff = Math.abs(referenceIsoR - grade.getIsoR());
                if (matchingGradeId == GRADE_UNSET || gradeDiff < matchingGradeDiff) {
                    matchingGradeId = gradeId;
                    matchingGradeDiff = gradeDiff;
                }
            }
        }

        if (matchingGradeId != GRADE_UNSET) {
            Log.d(TAG, "Matching grade ID is " + matchingGradeId + " with difference of " + matchingGradeDiff);
        }

        return matchingGradeId;
    }

    private int findDefaultGradeId() {
        final int[] gradeFallbackList = new int[] {
                PaperProfile.GRADE_2, PaperProfile.GRADE_3,
                PaperProfile.GRADE_NONE,
                PaperProfile.GRADE_0, PaperProfile.GRADE_00,
                PaperProfile.GRADE_4, PaperProfile.GRADE_5 };
        int gradeId = 0;
        for (int value : gradeFallbackList) {
            int index = findGradeIndex(value);
            if (index >= 0) {
                gradeId = value;
                break;
            }
        }
        return gradeId;
    }

    private int findGradeIndex(int gradeId) {
        int index = -1;
        for (int i = gradeIdList.size() - 1; i >= 0; --i) {
            if (gradeIdList.get(i) == gradeId) {
                index = i;
                break;
            }
        }
        return index;
    }
}
