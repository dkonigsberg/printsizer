package org.logicprobe.printsizer.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.LiveDataUtil;
import org.logicprobe.printsizer.R;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;
import org.logicprobe.printsizer.model.PaperProfile;
import org.logicprobe.printsizer.ui.Converter;

import java.util.ArrayList;
import java.util.List;

public class PaperSettingsDialogViewModel extends AndroidViewModel {
    private static final String TAG = PaperSettingsDialogViewModel.class.getSimpleName();
    private static final PaperProfile DEFAULT_PAPER_PROFILE = new PaperProfileEntity();
    private static final String PAPER_PROFILE_ID_KEY = "paper_profile_id";
    private static final String PAPER_GRADE_ID_KEY = "paper_grade_id";

    private final SavedStateHandle state;
    private final DataRepository repository;

    private MediatorLiveData<PaperProfile> paperProfile;
    private LiveData<PaperProfileEntity> loadedPaperProfile;

    private MutableLiveData<Integer> numGrades;
    private MutableLiveData<Integer> gradeIndex;
    private MutableLiveData<Integer> gradeLabelResourceId;
    private List<Integer> gradeIdList;

    public PaperSettingsDialogViewModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.state = savedStateHandle;
        this.repository = ((App)application).getRepository();
        this.paperProfile = new MediatorLiveData<>();
        this.numGrades = new MutableLiveData<>();
        this.gradeIndex = new MutableLiveData<>();
        this.gradeLabelResourceId = new MutableLiveData<>(R.string.empty);
        this.gradeIdList = new ArrayList<>();

        paperProfile.setValue(DEFAULT_PAPER_PROFILE);
        paperProfile.addSource(state.getLiveData(PAPER_PROFILE_ID_KEY, 0), new Observer<Integer>() {
            @Override
            public void onChanged(Integer profileId) {
                if (loadedPaperProfile != null) {
                    paperProfile.removeSource(loadedPaperProfile);
                    loadedPaperProfile = null;
                }
                if (profileId == 0) {
                    paperProfile.setValue(DEFAULT_PAPER_PROFILE);
                    updatePaperProperties();
                    return;
                }
                loadedPaperProfile = repository.loadPaperProfile(profileId);
                paperProfile.addSource(loadedPaperProfile, new Observer<PaperProfileEntity>() {
                    @Override
                    public void onChanged(PaperProfileEntity paperProfileEntity) {
                        if (paperProfileEntity == null) {
                            paperProfile.setValue(DEFAULT_PAPER_PROFILE);
                        } else {
                            paperProfile.setValue(paperProfileEntity);
                        }
                        updatePaperProperties();
                    }
                });
            }
        });
    }

    public void setPaperProfileId(int paperProfileId) {
        state.set(PAPER_PROFILE_ID_KEY, paperProfileId);
        updatePaperProperties();
    }

    public LiveData<PaperProfile> getPaperProfile() {
        return paperProfile;
    }

    public void setPaperGrade(@PaperProfile.GradeId int gradeId) {
        state.set(PAPER_GRADE_ID_KEY, gradeId);
        updateGradeProperties(false);
    }

    public LiveData<Integer> getPaperGrade() {
        return state.getLiveData(PAPER_GRADE_ID_KEY, PaperProfile.GRADE_NONE);
    }

    public LiveData<Integer> getNumGrades() {
        return numGrades;
    }

    public void setGradeIndex(int index) {
        setPaperGrade(gradeIdList.get(index));
    }

    public LiveData<Integer> getGradeIndex() {
        return gradeIndex;
    }

    public LiveData<Integer> getGradeLabelResourceId() {
        return gradeLabelResourceId;
    }

    private void updatePaperProperties() {
        PaperProfile profileValue = paperProfile.getValue();
        if (profileValue == null) { return; }

        gradeIdList.clear();
        if (profileValue.getGradeNone() != null && profileValue.getGradeNone().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_NONE);
        }
        if (profileValue.getGrade00() != null && profileValue.getGrade00().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_00);
        }
        if (profileValue.getGrade0() != null && profileValue.getGrade0().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_0);
        }
        if (profileValue.getGrade1() != null && profileValue.getGrade1().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_1);
        }
        if (profileValue.getGrade2() != null && profileValue.getGrade2().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_2);
        }
        if (profileValue.getGrade3() != null && profileValue.getGrade3().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_3);
        }
        if (profileValue.getGrade4() != null && profileValue.getGrade4().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_4);
        }
        if (profileValue.getGrade5() != null && profileValue.getGrade5().getIsoP() > 0) {
            gradeIdList.add(PaperProfile.GRADE_5);
        }
        numGrades.setValue(gradeIdList.size() - 1);
        updateGradeProperties(true);
    }

    private void updateGradeProperties(boolean paperChange) {
        int gradeId = LiveDataUtil.getIntValue(getPaperGrade());
        int index = findGradeIndex(gradeId);

        if (index < 0) {
            if (paperChange) {
                gradeId = findDefaultGradeId();
                state.set(PAPER_GRADE_ID_KEY, gradeId);
                index = findGradeIndex(gradeId);
                if (index < 0) {
                    index = 0;
                }
            } else {
                index = 0;
            }
        }

        int resourceId = Converter.paperGradeToResourceId(gradeId);
        gradeIndex.setValue(index);
        gradeLabelResourceId.setValue(resourceId);
    }

    private int findDefaultGradeId() {
        int[] gradeFallbackList = new int[] {
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
