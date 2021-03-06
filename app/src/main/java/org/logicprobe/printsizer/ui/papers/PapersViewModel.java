package org.logicprobe.printsizer.ui.papers;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

public class PapersViewModel extends AndroidViewModel {

    private final DataRepository repository;

    private LiveData<List<PaperProfileEntity>> paperProfiles;

    public PapersViewModel(Application application) {
        super(application);

        repository = ((App)application).getRepository();

        paperProfiles = repository.getPaperProfiles();
    }

    public LiveData<List<PaperProfileEntity>> getPaperProfiles() {
        return paperProfiles;
    }
}