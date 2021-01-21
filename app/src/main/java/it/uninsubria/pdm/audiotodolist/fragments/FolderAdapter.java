package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.entity.Folder;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder>{
    private final LayoutInflater inflater;
    private List<Folder> folderList;

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView folderImg;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            folderImg = itemView.findViewById(R.id.imageView);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getFolderImg() {
            return folderImg;
        }
    }

    public FolderAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.folder_view_row, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        holder.getTextView().setText(folderList.get(position).folderName);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public List<Folder> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<Folder> folderList) {
        this.folderList = folderList;
    }
}
