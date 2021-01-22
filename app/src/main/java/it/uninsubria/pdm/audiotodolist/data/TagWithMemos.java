package it.uninsubria.pdm.audiotodolist.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

public class TagWithMemos {
    @Embedded public Tag tag;
    @Relation(parentColumn = "tagName",
                entityColumn = "title",
                associateBy = @Junction(VoiceMemoCrossTags.class)) public List<VoiceMemo> memos;
}
