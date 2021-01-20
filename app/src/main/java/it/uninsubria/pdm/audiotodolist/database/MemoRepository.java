package it.uninsubria.pdm.audiotodolist.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

/**
 * Class that is responsible for the access to multiple tables of the database.
 */
public class MemoRepository {

    private MemoDAO memoDAO;
    private TagDAO tagDAO;
    private LiveData<List<VoiceMemo>> allMemo;

    MemoRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        memoDAO = db.memoDAO();
        tagDAO = db.tagDAO();
        allMemo = memoDAO.readAllData();
    }

}
