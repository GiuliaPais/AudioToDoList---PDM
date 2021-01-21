package it.uninsubria.pdm.audiotodolist.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

/**
 * Implementation of the app database (via Room).
 * Implements the singleton pattern.
 */
@Database(entities = {VoiceMemo.class, Tag.class, Folder.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;
    private static RoomDatabase.Callback prePopulateCallBack = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateFoldersAsync(instance).execute();
        }
    };

    /**
     * Gets the only instance of this database if it exists, if not creates one.
     * @param context the context
     * @return an AppDatabase instance
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "AudioToDoListDB")
                    .addCallback(prePopulateCallBack)
                    .build();
        }
        return instance;
    }

    /**
     * Gets the DAO associated with the VoiceMemo table.
     * @return a MemoDAO
     */
    public abstract MemoDAO memoDAO();

    /**
     * Gets the DAO associated with the Tag table.
     * @return a TagDAO
     */
    public abstract TagDAO tagDAO();

    public abstract FolderDAO folderDAO();
}
