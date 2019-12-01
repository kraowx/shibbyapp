package io.github.kraowx.shibbyapp.ui.allfiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AllFilesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AllFilesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}