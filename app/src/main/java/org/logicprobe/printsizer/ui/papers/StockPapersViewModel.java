package org.logicprobe.printsizer.ui.papers;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.PaperProfileEntity;

import java.util.List;

public class StockPapersViewModel extends AndroidViewModel {

    private final DataRepository repository;

    private LiveData<List<PaperProfileEntity>> paperProfiles;

    public StockPapersViewModel(Application application) {
        super(application);

        repository = ((App)application).getRepository();

        paperProfiles = repository.getStockPaperProfiles();
    }

    public LiveData<List<PaperProfileEntity>> getStockPaperProfiles() {
        return paperProfiles;
    }
}
