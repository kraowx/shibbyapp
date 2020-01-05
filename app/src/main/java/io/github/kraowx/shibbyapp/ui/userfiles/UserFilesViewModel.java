package io.github.kraowx.shibbyapp.ui.userfiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UserFilesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public UserFilesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is share fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}