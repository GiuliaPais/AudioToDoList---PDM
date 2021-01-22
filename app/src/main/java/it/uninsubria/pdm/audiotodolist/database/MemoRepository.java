package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

/**
 * Class that is responsible for the access to multiple tables of the database.
 */
public class MemoRepository {

    private static class InsertFolderAsyncTask extends AsyncTask<Folder, Void, Void> {
        private FolderDAO folderDaoAsync;

        public InsertFolderAsyncTask(FolderDAO folderDaoAsync) {
            this.folderDaoAsync = folderDaoAsync;
        }

        @Override
        protected Void doInBackground(Folder... folders) {
            folderDaoAsync.insert(folders);
            return null;
        }
    }

    private static class DeleteFolderAsyncTask extends AsyncTask<String, Void, Void> {
        private FolderDAO folderDaoAsync;

        public DeleteFolderAsyncTask(FolderDAO folderDaoAsync) {
            this.folderDaoAsync = folderDaoAsync;
        }

        @Override
        protected Void doInBackground(String... strings) {
            folderDaoAsync.deleteFolderByName(strings[0]);
            return null;
        }
    }

    private MemoDAO memoDAO;
    private TagDAO tagDAO;
    private FolderDAO folderDAO;
    private VoiceMemoCrossTagsDAO voiceMemoCrossTagsDAO;
    private LiveData<List<Folder>> allFolders;
    private LiveData<List<MemoWithTags>> allMemos;

    MemoRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        memoDAO = db.memoDAO();
        tagDAO = db.tagDAO();
        folderDAO = db.folderDAO();
        voiceMemoCrossTagsDAO = db.voiceMemoCrossTagsDAO();
        allFolders = folderDAO.getAllFolders();
        allMemos = voiceMemoCrossTagsDAO.getAllMemosWithTags();
    }

    public LiveData<List<Folder>> getAllFolders() {
        return allFolders;
    }

    public void insertFolder(Folder... folders) {
        new InsertFolderAsyncTask(folderDAO).execute(folders);
    }

    public void deleteFolder(String folderName) {
        new DeleteFolderAsyncTask(folderDAO).execute(folderName);
    }

    public LiveData<List<MemoWithTags>> getAllMemosInFolder(String folderName) {
        if (folderName.equals("ALL")) {
            return allMemos;
        } else {
            return voiceMemoCrossTagsDAO.getMemosWithTagsByFolder(folderName);
        }
    }

}
