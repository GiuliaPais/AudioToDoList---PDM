package it.uninsubria.pdm.audiotodolist.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Duration;
import java.time.LocalDateTime;


@Entity
public class VoiceMemo {
    @PrimaryKey
    public String title;
    public LocalDateTime dateTime;
    public Duration duration;
    public String path;
    public String comment;
    public String folder;
}
