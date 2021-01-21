package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

/**
 * Class that is responsible for the access to multiple tables of the database.
 */
public class MemoRepository {

    private MemoDAO memoDAO;
    private TagDAO tagDAO;
    private FolderDAO folderDAO;
    private LiveData<List<VoiceMemo>> allMemo;
    private LiveData<List<Folder>> allFolders;

    MemoRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        memoDAO = db.memoDAO();
        tagDAO = db.tagDAO();
        folderDAO = db.folderDAO();
        allMemo = memoDAO.readAllData();
        allFolders = folderDAO.getAllFolders();
    }

    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }

}
