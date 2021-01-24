package it.uninsubria.pdm.audiotodolist.data;

import it.uninsubria.pdm.audiotodolist.R;

public enum DefaultFolders {

    ALL(R.string.all_notes);

    private int resID;

    DefaultFolders(int title_res_id) {
       resID = title_res_id;
    }

    public int getResID() {
        return resID;
    }
}
