package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private MutableLiveData<String> selectedFolder = new MutableLiveData<>();


    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        //memorep.getall
    }
}
