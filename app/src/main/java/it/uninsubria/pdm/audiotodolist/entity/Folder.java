package it.uninsubria.pdm.audiotodolist.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Folder {
    @NonNull
    @PrimaryKey
    public String folderName;

    public Folder(@NonNull String folderName) {
        this.folderName = folderName;
    }
}
