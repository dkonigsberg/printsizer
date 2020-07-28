package org.logicprobe.printsizer.ui.home;

import android.app.Application;

import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.logicprobe.printsizer.App;
import org.logicprobe.printsizer.DataRepository;
import org.logicprobe.printsizer.db.entity.EnlargerProfileEntity;

import java.util.ArrayList;
import java.util.List;

public class ChooseEnlargerDialogViewModel extends AndroidViewModel {
    private final DataRepository repository;
    private final LiveData<List<EnlargerProfileEntity>> enlargerProfiles;
    private final ChooseEnlargerElement actionAddElement;
    private final LiveData<List<ChooseEnlargerElement>> selectionList;

    public ChooseEnlargerDialogViewModel(Application application) {
        super(application);

        repository = ((App)application).getRepository();

        actionAddElement = ChooseEnlargerElement.createAction(1);
        enlargerProfiles = repository.getEnlargerProfiles();

        selectionList = Transformations.switchMap(enlargerProfiles, new Function<List<EnlargerProfileEntity>, LiveData<List<ChooseEnlargerElement>>>() {
            @Override
            public LiveData<List<ChooseEnlargerElement>> apply(List<EnlargerProfileEntity> input) {
                List<ChooseEnlargerElement> elementList = new ArrayList<>((input != null ? input.size() : 0) + 1);
                if (input != null && input.size() > 0) {
                    for (EnlargerProfileEntity element : input) {
                        elementList.add(ChooseEnlargerElement.create(element));
                    }
                }
                elementList.add(actionAddElement);
                return new MutableLiveData<>(elementList);
            }
        });
    }

    public LiveData<List<ChooseEnlargerElement>> getSelectionList() {
        return selectionList;
    }
}
