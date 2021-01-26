package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

@Dao
public interface VoiceMemoCrossTagsDAO {
    @Transaction
    @Query("SELECT * FROM VOICEMEMO")
    LiveData<List<MemoWithTags>> getAllMemosWithTags();

    @Transaction
    @Query("SELECT * FROM VOICEMEMO WHERE FOLDER = :folderName")
    LiveData<List<MemoWithTags>> getMemosWithTagsByFolder(String folderName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(VoiceMemoCrossTags... voiceMemoCrossTags);

    @Transaction
    @Query("SELECT * FROM VOICEMEMO")
    List<MemoWithTags> getAllMemosWithTagsFlat();

    @Transaction
    @Query("SELECT * FROM VOICEMEMO WHERE TITLE = :title")
    LiveData<MemoWithTags> getMemoWithTags(String title);

    @Delete
    void delete(VoiceMemoCrossTags... voiceMemoCrossTags);

    @Query("SELECT * FROM VOICEMEMOCROSSTAGS")
    List<VoiceMemoCrossTags> readAllMappings();

    @Query("SELECT DISTINCT * " +
            "FROM VOICEMEMO " +
            "WHERE FOLDER = :folder AND TITLE IN " +
            "(" +
            "SELECT DISTINCT TITLE " +
            "FROM VOICEMEMOCROSSTAGS " +
            "WHERE TAGNAME IN (:tags)" +
            ")")
    LiveData<List<MemoWithTags>> getMemosByFolderAndTag(String folder, String... tags);
}
