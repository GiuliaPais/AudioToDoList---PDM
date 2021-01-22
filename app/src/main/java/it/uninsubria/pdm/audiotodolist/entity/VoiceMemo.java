package it.uninsubria.pdm.audiotodolist.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;


@Entity(foreignKeys = @ForeignKey(entity = Folder.class,
        parentColumns = "folderName", childColumns = "folder", onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE))
public class VoiceMemo implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(index = true)
    public String title;
    public LocalDateTime dateTime;
    public Duration duration;
    @NonNull
    public String path;
    public String comment;
    @NonNull
    @ColumnInfo(index = true)
    public String folder;
}
