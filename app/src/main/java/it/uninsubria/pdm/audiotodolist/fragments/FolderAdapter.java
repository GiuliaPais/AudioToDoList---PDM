package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.entity.Folder;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder>{
    private final LayoutInflater inflater;
    private List<Folder> folderList;
    private static OnItemClickListener listener;

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView folderImg;
        private final ConstraintLayout viewBackground, viewForeground;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            folderImg = itemView.findViewById(R.id.imageView);
            viewForeground = itemView.findViewById(R.id.folder_item_foreground);
            viewBackground = itemView.findViewById(R.id.folder_item_background);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(itemView, position);
                    }
                }
            });
        }

        public TextView getTextView() {
            return textView;
        }

        public ConstraintLayout getViewForeground() {
            return viewForeground;
        }
    }
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
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
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
