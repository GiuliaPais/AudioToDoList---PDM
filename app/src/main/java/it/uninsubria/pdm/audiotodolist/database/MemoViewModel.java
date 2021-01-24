package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.concurrent.ExecutionException;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private MutableLiveData<String> selectedFolder = new MutableLiveData<>(DefaultFolders.ALL.name());
    private LiveData<List<Folder>> allFolders;
    private LiveData<List<MemoWithTags>> visibleMemos;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        allFolders = memoRepository.getAllFolders();
        visibleMemos = Transformations.switchMap(selectedFolder, (folder) -> memoRepository.getAllMemosInFolder(folder));
    }

    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }

    public void createNewFolder(Folder folder) {
        memoRepository.insertFolder(folder);
    }

    public void deleteFolder(String folderName) {
        memoRepository.deleteFolder(folderName);
    }

    public void setSelectedFolder(String folderName) {
        selectedFolder.setValue(folderName);
    }

    public MutableLiveData<String> getSelectedFolder() {
        return selectedFolder;
    }

    public LiveData<List<MemoWithTags>> getVisibleMemos() {
        return visibleMemos;
    }

    public Long createNewMemo(VoiceMemo voiceMemo) throws ExecutionException, InterruptedException {
        return memoRepository.insertVoiceMemo(voiceMemo);
    }

    public void registerMemoChanges(MemoWithTags old, MemoWithTags newMemo) {
        memoRepository.registerMemoChanges(old, newMemo);
    }

    public void deleteMemo(String memoTitle) {
        memoRepository.deleteMemo(memoTitle);
    }
}
