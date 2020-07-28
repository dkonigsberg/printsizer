package org.logicprobe.printsizer.ui.enlargers;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.List;

public class EnlargersViewModel extends AndroidViewModel {

    private final DataRepository repository;

    private LiveData<List<EnlargerProfileEntity>> enlargerProfiles;

    public EnlargersViewModel(Application application) {
        super(application);

        repository = ((App)application).getRepository();

        enlargerProfiles = repository.getEnlargerProfiles();
    }

    public LiveData<List<EnlargerProfileEntity>> getEnlargerProfiles() {
        return enlargerProfiles;
    }
}