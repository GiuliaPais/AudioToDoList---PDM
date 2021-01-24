package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
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

    private static class InsertVoiceMemoAsyncTask extends AsyncTask<VoiceMemo, Void, Long> {
        private MemoDAO memoDAO;

        public InsertVoiceMemoAsyncTask(MemoDAO memoDAO) {
            this.memoDAO = memoDAO;
        }

        @Override
        protected Long doInBackground(VoiceMemo... voiceMemos) {
            VoiceMemo toInsert = voiceMemos[0];
            //On conflict try to obtain a unique title and try insertion again
            long insertedId = memoDAO.insertMemo(toInsert);
            int i = 1;
            while (insertedId == -1) {
                String newTitle = toInsert.title + " (" + i + ")";
                toInsert.title = newTitle;
                i++;
                insertedId = memoDAO.insertMemo(toInsert);
            }
            return insertedId;
        }
    }

    private static class UpdateVoiceMemoAsyncTask extends AsyncTask<VoiceMemo, Void, Void> {
        private MemoDAO memoDAO;

        public UpdateVoiceMemoAsyncTask(MemoDAO memoDAO) {
            this.memoDAO = memoDAO;
        }

        @Override
        protected Void doInBackground(VoiceMemo... voiceMemos) {
            memoDAO.updateMemo(voiceMemos);
            return null;
        }
    }

    private static class ChangeMemoPkAsyncTask extends AsyncTask<VoiceMemo, Void, Long> {
        private AppDatabase db;
        private MemoDAO memoDAO;

        public ChangeMemoPkAsyncTask(AppDatabase database, MemoDAO memoDAO) {
            this.db = database;
            this.memoDAO = memoDAO;
        }

        @Override
        protected Long doInBackground(VoiceMemo... voiceMemos) {
            VoiceMemo toDelete = voiceMemos[0];
            VoiceMemo toInsert = voiceMemos[1];
            db.runInTransaction(() -> {
                long insertedId = memoDAO.insertMemo(toInsert);
                int i = 1;
                while (insertedId == -1) {
                    String newTitle = toInsert.title + " (" + i + ")";
                    toInsert.title = newTitle;
                    i++;
                    insertedId = memoDAO.insertMemo(toInsert);
                }
                memoDAO.deleteMemo(toDelete);
            });
            return null;
        }
    }

    private static class DeleteMemoAsyncTask extends AsyncTask<String, Void, Void> {
        private MemoDAO memoDAO;

        public DeleteMemoAsyncTask(MemoDAO memoDAO) {
            this.memoDAO = memoDAO;
        }

        @Override
        protected Void doInBackground(String... strings) {
            memoDAO.deleteByName(strings[0]);
            return null;
        }
    }


    private AppDatabase db;
    private MemoDAO memoDAO;
    private TagDAO tagDAO;
    private FolderDAO folderDAO;
    private VoiceMemoCrossTagsDAO voiceMemoCrossTagsDAO;
    private LiveData<List<Folder>> allFolders;
    private LiveData<List<MemoWithTags>> allMemos;

    MemoRepository(Application application) {
        db = AppDatabase.getInstance(application);
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
        if (folderName.equals(DefaultFolders.ALL.name())) {
            return allMemos;
        } else {
            return voiceMemoCrossTagsDAO.getMemosWithTagsByFolder(folderName);
        }
    }

    public Long insertVoiceMemo(VoiceMemo memo) throws ExecutionException, InterruptedException {
        return new InsertVoiceMemoAsyncTask(memoDAO).execute(memo).get();
    }

    public void registerMemoChanges(MemoWithTags old, MemoWithTags newMemo) {
        //Check if the newMemo has a different title (different primary key)
        boolean sameTitle = old.voiceMemo.title.equals(newMemo.voiceMemo.title);
        if (sameTitle) {
            new UpdateVoiceMemoAsyncTask(memoDAO).execute(newMemo.voiceMemo);
        } else {
            changeMemoPK(old.voiceMemo, newMemo.voiceMemo);
        }
        //Check the tags and register eventually new tags
        if (newMemo.tagList == null || newMemo.tagList.isEmpty()) {
            return;
        }
        tagDAO.insertTag(newMemo.tagList.toArray(new Tag[newMemo.tagList.size()]));
        //If there are tags register mappings
        List<VoiceMemoCrossTags> mappings = new ArrayList<>();
        for (Tag tag : newMemo.tagList) {
            VoiceMemoCrossTags x = new VoiceMemoCrossTags();
            x.title = newMemo.voiceMemo.title;
            x.tagName = tag.tagName;
            mappings.add(x);
        }
        voiceMemoCrossTagsDAO.insert(mappings.toArray(new VoiceMemoCrossTags[mappings.size()]));

    }

    private void changeMemoPK(VoiceMemo old, VoiceMemo newM) {
        new ChangeMemoPkAsyncTask(db, memoDAO).execute(old, newM);
    }

    public void deleteMemo(String memoTitle) {
        new DeleteMemoAsyncTask(memoDAO).execute(memoTitle);
    }
}
