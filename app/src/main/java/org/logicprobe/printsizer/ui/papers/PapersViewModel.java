package org.logicprobe.printsizer.ui.papers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PapersViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PapersViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the paper profiles fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}