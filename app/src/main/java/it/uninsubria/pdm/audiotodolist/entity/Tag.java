package it.uninsubria.pdm.audiotodolist.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;


@Entity(primaryKeys = {"tagName", "memoTitle"}, foreignKeys = @ForeignKey(entity = VoiceMemo.class,
                        parentColumns = "title", childColumns = "memoTitle", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE))
public class Tag {
    @NonNull
    public String tagName;
    @NonNull
    @ColumnInfo(index = true)
    public String memoTitle;
}
