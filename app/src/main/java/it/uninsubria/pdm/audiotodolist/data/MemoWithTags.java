package it.uninsubria.pdm.audiotodolist.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemoCrossTags;

public class MemoWithTags implements Serializable {
    @Embedded public VoiceMemo voiceMemo;
    @Relation(parentColumn = "title",
            entityColumn = "tagName",
            associateBy = @Junction(VoiceMemoCrossTags.class)) public List<Tag> tagList;
}
