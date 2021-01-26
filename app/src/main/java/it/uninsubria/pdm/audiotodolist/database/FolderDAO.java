package it.uninsubria.pdm.audiotodolist.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Folder;

@Dao
public interface FolderDAO {
    @Query("SELECT * FROM FOLDER WHERE FOLDERNAME != 'ALL'")
    LiveData<List<Folder>> getAllFolders();

    @Insert
    void insert(Folder... folder);

    @Delete
    void delete(Folder... folder);

    @Query("DELETE FROM FOLDER WHERE FOLDERNAME = :folderName")
    void deleteFolderByName(String folderName);
}
