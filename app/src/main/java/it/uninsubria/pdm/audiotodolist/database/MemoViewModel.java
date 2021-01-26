package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoViewModel extends AndroidViewModel {
    private MemoRepository memoRepository;
    private MutableLiveData<String> selectedFolder = new MutableLiveData<>(DefaultFolders.ALL.name());
    private MutableLiveData<List<String>> selectedTags = new MutableLiveData<>(new ArrayList<>());
    private MediatorLiveData<Pair<String, List<String>>> folderAndTags = new MediatorLiveData<>();
    private LiveData<List<Folder>> allFolders;
    private LiveData<List<MemoWithTags>> visibleMemos;
    private LiveData<List<Tag>> allTags;

    //For single Memo
    private MutableLiveData<String> memoTitle = new MutableLiveData<>();
    private LiveData<MemoWithTags> currentMemo;

    public MemoViewModel(@NonNull Application application) {
        super(application);
        memoRepository = new MemoRepository(application);
        allFolders = memoRepository.getAllFolders();
        folderAndTags.addSource(selectedFolder, s -> {
            Pair<String, List<String>> newPair = new Pair<>(s, selectedTags.getValue());
            folderAndTags.setValue(newPair);
        });
        folderAndTags.addSource(selectedTags, t -> {
            Pair<String, List<String>> newPair = new Pair<>(selectedFolder.getValue(), t);
            folderAndTags.setValue(newPair);
        });
        visibleMemos = Transformations.switchMap(folderAndTags, (pair) -> memoRepository.getAllMemosInFolderWithTags(pair.first, pair.second));
        currentMemo = Transformations.switchMap(memoTitle, (title) -> memoRepository.getSelectedMemo(title));
        allTags = memoRepository.getAllTags();
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
        memoTitle.setValue(newMemo.voiceMemo.title);
    }

    public void deleteMemo(String memoTitle) {
        memoRepository.deleteMemo(memoTitle);
    }

    public LiveData<MemoWithTags> getCurrentMemo() {
        return currentMemo;
    }

    public void setMemoTitle(String memoTitle) {
        this.memoTitle.setValue(memoTitle);
    }

    public void addTagsToMemo(MemoWithTags memo, Tag... tags) {
        if (tags != null) {
            if (tags.length == 0) {
                return;
            }
            memoRepository.insertTags(tags);
            memoRepository.addTagsToMemo(memo.voiceMemo, tags);
        }
    }

    public void deleteTagsFromMemo(MemoWithTags memo, Tag... tags) {
        if (tags != null) {
            if (tags.length == 0) {
                return;
            }
            memoRepository.removeTagsFromMemo(memo.voiceMemo, tags);
        }
    }

    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public void clearSelectedTags() {
        selectedTags.setValue(new ArrayList<>());
    }

    public void addToTagSelection(String tag) {
        List<String> current = selectedTags.getValue();
        current.add(tag);
        selectedTags.setValue(current);
    }

    public void removeTagFromSelection(String tag) {
        List<String> current = selectedTags.getValue();
        current.remove(tag);
        selectedTags.setValue(current);
    }
}
