package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

@Dao
public interface MemoDAO {
    /**
     * Loads all the voice memos in the table.
     * @return observable list of all voice memos
     */
    @Query("SELECT * FROM VOICEMEMO")
    LiveData<List<VoiceMemo>> readAllData();

    /**
     * Loads all the voice memos in the selected folder.
     * @param folder the folder name
     * @return observable list of voice memos
     */
    @Query("SELECT * FROM VOICEMEMO WHERE FOLDER LIKE :folder")
    LiveData<List<VoiceMemo>> readAllDataInFolder(String folder);

    /**
     * Inserts one or more new voice memos in the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Insert
    void insertMemo(VoiceMemo... voiceMemos);

    /**
     * Deletes one or more voice memos from the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Delete
    void deleteMemo(VoiceMemo... voiceMemos);

    /**
     * Updates one or more voice memos in the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Update
    void updateMemo(VoiceMemo... voiceMemos);
}
