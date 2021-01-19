package it.uninsubria.pdm.audiotodolist.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

@Database(entities = {VoiceMemo.class, Tag.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private AppDatabase instance;
    public AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (this) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, "AudioToDoListDB").build();
            }
        }
        return instance;
    }
    public abstract MemoDAO memoDAO();
}
