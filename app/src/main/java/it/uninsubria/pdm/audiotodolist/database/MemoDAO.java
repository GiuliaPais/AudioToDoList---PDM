package it.uninsubria.pdm.audiotodolist.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

@Dao
public interface MemoDAO {
    /**
     * Loads all the voice memos in the selected folder.
     * @param folder the folder name
     * @return observable list of voice memos
     */
    @Query("SELECT * FROM VOICEMEMO WHERE FOLDER LIKE :folder")
    List<VoiceMemo> readAllDataInFolder(String folder);

    /**
     * Inserts one or more new voice memos in the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertMemo(VoiceMemo voiceMemos);

    /**
     * Deletes one or more voice memos from the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Delete
    void deleteMemo(VoiceMemo... voiceMemos);

    @Query("DELETE FROM VOICEMEMO WHERE TITLE = :name")
    void deleteByName(String name);

    /**
     * Updates one or more voice memos in the table.
     * @param voiceMemos one or more VoiceMemo objects
     */
    @Update
    void updateMemo(VoiceMemo... voiceMemos);
}
