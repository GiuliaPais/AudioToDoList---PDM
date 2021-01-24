package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Tag;

/**
 * DAO for the Tag table.
 */
@Dao
public interface TagDAO {
    @Query("SELECT * FROM TAG")
    LiveData<List<Tag>> getAllTags();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTag(Tag... tag);

    @Delete
    void deleteTag(Tag tag);
}
