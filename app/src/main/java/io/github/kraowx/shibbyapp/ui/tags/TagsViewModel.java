package io.github.kraowx.shibbyapp.ui.tags;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TagsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public TagsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}