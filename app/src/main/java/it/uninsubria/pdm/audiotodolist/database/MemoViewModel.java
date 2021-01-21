package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private MutableLiveData<String> selectedFolder = new MutableLiveData<>();
    private LiveData<List<Folder>> allFolders;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        allFolders = memoRepository.getAllFolders();
        //memorep.getall
    }

    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }
}
