package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.data.TagWithMemos;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

@Dao
public interface VoiceMemoCrossTagsDAO {
    @Transaction
    @Query("SELECT * FROM VOICEMEMO")
    LiveData<List<MemoWithTags>> getAllMemosWithTags();

    @Transaction
    @Query("SELECT * FROM TAG")
    LiveData<List<TagWithMemos>> getAllTagsWithMemos();

    @Transaction
    @Query("SELECT * FROM VOICEMEMO WHERE FOLDER = :folderName")
    LiveData<List<MemoWithTags>> getMemosWithTagsByFolder(String folderName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(VoiceMemoCrossTags... voiceMemoCrossTags);

    @Transaction
    @Query("SELECT * FROM VOICEMEMO")
    List<MemoWithTags> getAllMemosWithTagsFlat();
}
