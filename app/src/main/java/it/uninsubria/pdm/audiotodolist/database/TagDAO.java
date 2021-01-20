package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Tag;

/**
 * DAO for the Tag table.
 */
@Dao
public interface TagDAO {
    /**
     * Gets all the tags associated with the given memo.
     * @param memoTitle the title of the memo
     * @return Observable list of tag names
     */
    @Query("SELECT TAGNAME FROM TAG WHERE MEMOTITLE LIKE :memoTitle")
    LiveData<List<String>> getAllTags(String memoTitle);

    @Insert
    void insertTag(Tag tag);

    @Delete
    void deleteTag(Tag tag);
}
