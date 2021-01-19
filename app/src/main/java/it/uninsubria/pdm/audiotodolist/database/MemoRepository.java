package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoRepository {

    private MemoDAO memoDAO;
    private LiveData<List<VoiceMemo>>  allMemo = memoDAO.readAllData();

    public MemoRepository(MemoDAO memoDAO) {
        this.memoDAO = memoDAO;
    }

}
