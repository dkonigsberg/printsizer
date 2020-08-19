package org.logicprobe.printsizer.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

public class ChoosePaperDialogViewModel extends AndroidViewModel {
    private final DataRepository repository;
    private final LiveData<List<PaperProfileEntity>> paperProfiles;

    public ChoosePaperDialogViewModel(Application application) {
        super(application);

        repository = ((App)application).getRepository();

        paperProfiles = repository.getPaperProfiles();
    }

    public LiveData<List<PaperProfileEntity>> getPaperProfiles() {
        return paperProfiles;
    }
}
