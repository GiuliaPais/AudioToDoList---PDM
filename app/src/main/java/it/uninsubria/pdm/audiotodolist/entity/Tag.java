package it.uninsubria.pdm.audiotodolist.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"tagName", "memoTitle"}, foreignKeys = @ForeignKey(entity = VoiceMemo.class,
                        parentColumns = "title", childColumns = "memoTitle", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE))
public class Tag {
    public String tagName;
    public String memoTitle;
}
