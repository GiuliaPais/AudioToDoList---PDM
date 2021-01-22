package it.uninsubria.pdm.audiotodolist.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"tagName", "title"})
public class VoiceMemoCrossTags {
    @NonNull
    public String tagName;
    @NonNull
    @ColumnInfo(index = true)
    public String title;
}
