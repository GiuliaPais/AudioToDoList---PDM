package it.uninsubria.pdm.audiotodolist.database;

import android.os.AsyncTask;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.entity.Folder;

public class PopulateFoldersAsync extends AsyncTask<Void, Void, Void> {

    private static final String[] defaultFolders = {DefaultFolders.ALL.name()};
    private final FolderDAO dao;

    public PopulateFoldersAsync(AppDatabase db) {
        dao = db.folderDAO();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (String def : defaultFolders) {
            Folder f = new Folder(def);
            dao.insert(f);
        }
        return null;
    }
}
