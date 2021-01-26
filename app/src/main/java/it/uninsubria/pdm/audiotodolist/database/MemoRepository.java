package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.io.File;
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

    private static class DeleteFilesAsync extends AsyncTask<String, Void, Boolean> {
        private MemoDAO memoDAO;

        public DeleteFilesAsync(MemoDAO memoDAO) {
            this.memoDAO = memoDAO;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            List<VoiceMemo> memos = memoDAO.readAllDataInFolder(strings[0]);
            int deleted = 0;
            for (VoiceMemo v : memos) {
                File file = new File(v.path);
                if (file.exists()) {
                    deleted = file.delete() ? deleted++ : deleted;
                }
            }
            if (deleted == memos.size()) {
                return true;
            }
            return false;
        }
    }

    private static class InsertTagAsyncTask extends AsyncTask<Tag, Void, Void> {
        private TagDAO tagDAO;

        public InsertTagAsyncTask(TagDAO tagDAO) {
            this.tagDAO = tagDAO;
        }

        @Override
        protected Void doInBackground(Tag... tags) {
            tagDAO.insertTag(tags);
            return null;
        }
    }

    private static class InsertMemoTagsMappingsAsyncTask extends AsyncTask<VoiceMemoCrossTags, Void, Void> {
        private VoiceMemoCrossTagsDAO dao;

        public InsertMemoTagsMappingsAsyncTask(VoiceMemoCrossTagsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(VoiceMemoCrossTags... voiceMemoCrossTags) {
            dao.insert(voiceMemoCrossTags);
            return null;
        }
    }

    private static class DeleteMemoTagsMappingsAsyncTask extends AsyncTask<VoiceMemoCrossTags, Void, Void> {
        private VoiceMemoCrossTagsDAO dao;

        public DeleteMemoTagsMappingsAsyncTask(VoiceMemoCrossTagsDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(VoiceMemoCrossTags... voiceMemoCrossTags) {
            dao.delete(voiceMemoCrossTags);
            return null;
        }
    }

    private static class TagCleanupAsync extends AsyncTask<Void, Void, Void> {
        private TagDAO tagDAO;

        public TagCleanupAsync(TagDAO tagDAO) {
            this.tagDAO = tagDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            tagDAO.tagCleanup();
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
        new DeleteFilesAsync(memoDAO).execute(folderName);
        new DeleteFolderAsyncTask(folderDAO).execute(folderName);
    }

    public LiveData<List<MemoWithTags>> getAllMemosInFolderWithTags(String folderName, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            if (folderName.equals(DefaultFolders.ALL.name())) {
                return allMemos;
            } else {
                return voiceMemoCrossTagsDAO.getMemosWithTagsByFolder(folderName);
            }
        }
        return voiceMemoCrossTagsDAO.getMemosByFolderAndTag(folderName, tags.toArray(new String[0]));
    }

    public Long insertVoiceMemo(VoiceMemo memo) throws ExecutionException, InterruptedException {
        return new InsertVoiceMemoAsyncTask(memoDAO).execute(memo).get();
    }

    public LiveData<MemoWithTags> getSelectedMemo(String title) {
        return voiceMemoCrossTagsDAO.getMemoWithTags(title);
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
            return ;
        }
        new InsertTagAsyncTask(tagDAO).execute(newMemo.tagList.toArray(new Tag[0]));
        //If there are tags register mappings
        List<VoiceMemoCrossTags> mappings = new ArrayList<>();
        for (Tag tag : newMemo.tagList) {
            VoiceMemoCrossTags x = new VoiceMemoCrossTags();
            x.title = newMemo.voiceMemo.title;
            x.tagName = tag.tagName;
            mappings.add(x);
        }
        insertMappings(mappings.toArray(new VoiceMemoCrossTags[0]));
    }

    private void changeMemoPK(VoiceMemo old, VoiceMemo newM) {
        new ChangeMemoPkAsyncTask(db, memoDAO).execute(old, newM);
    }

    public void deleteMemo(String memoTitle) {
        new DeleteMemoAsyncTask(memoDAO).execute(memoTitle);
    }

    public void insertTags(Tag... tags) {
        new InsertTagAsyncTask(tagDAO).execute(tags);
    }

    private void insertMappings(VoiceMemoCrossTags... mappings) {
        new InsertMemoTagsMappingsAsyncTask(voiceMemoCrossTagsDAO).execute(mappings);
    }

    private void deleteMappings(VoiceMemoCrossTags... mappings) {
        new DeleteMemoTagsMappingsAsyncTask(voiceMemoCrossTagsDAO).execute(mappings);
    }

    public void addTagsToMemo(VoiceMemo memo, Tag... tags) {
        List<VoiceMemoCrossTags> mappings = new ArrayList<>();
        for (Tag tag : tags) {
            VoiceMemoCrossTags x = new VoiceMemoCrossTags();
            x.title = memo.title;
            x.tagName = tag.tagName;
            mappings.add(x);
        }
        insertMappings(mappings.toArray(new VoiceMemoCrossTags[0]));
    }

    public void removeTagsFromMemo(VoiceMemo memo, Tag... tags) {
        List<VoiceMemoCrossTags> mappings = new ArrayList<>();
        for (Tag tag : tags) {
            VoiceMemoCrossTags x = new VoiceMemoCrossTags();
            x.title = memo.title;
            x.tagName = tag.tagName;
            mappings.add(x);
        }
        deleteMappings(mappings.toArray(new VoiceMemoCrossTags[0]));
        new TagCleanupAsync(tagDAO).execute();
    }

    public LiveData<List<Tag>> getAllTags() {
        return tagDAO.getAllTags();
    }
}
