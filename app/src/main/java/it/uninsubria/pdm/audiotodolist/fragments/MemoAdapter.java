package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.MediaPlayerManager;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {
    private final LayoutInflater inflater;
    private Context context;
    private List<MemoWithTags> memoList = new ArrayList<>();
    private static OnItemClickListener listener;
    private static OnPlayButtonClickListener playListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnPlayButtonClickListener {
        void onPlayButtonClick(View itemView, int position);
    }


    /**
     * View holder for recycler view.
     */
    static class MemoViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, duration, currentTime, folder;
        private final MaterialButton playButton, stopButton;
        private final Slider slider;
        private final ConstraintLayout viewBackground, viewForeground;
        private Chip addTagChip;
        private ChipGroup chipGroup;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.memoTitle);
            playButton = itemView.findViewById(R.id.play);
            stopButton = itemView.findViewById(R.id.stop);
            slider = itemView.findViewById(R.id.slider);
            duration = itemView.findViewById(R.id.durationTrack2);
            currentTime = itemView.findViewById(R.id.durationTrack1);
            folder = itemView.findViewById(R.id.folderTextView);
            viewBackground = itemView.findViewById(R.id.memo_item_background);
            viewForeground = itemView.findViewById(R.id.memo_item_foreground);
            addTagChip = itemView.findViewById(R.id.addTagChip);
            chipGroup = itemView.findViewById(R.id.chipGroup);
        }

        public TextView getTitle() {
            return title;
        }

        public ConstraintLayout getViewForeground() {
            return viewForeground;
        }

        public TextView getFolder() {
            return folder;
        }

        public TextView getDuration() {
            return duration;
        }

        public MaterialButton getPlayButton() {
            return playButton;
        }

        public Chip getAddTagChip() {
            return addTagChip;
        }

        public ChipGroup getChipGroup() {
            return chipGroup;
        }
    }

    public MemoAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MemoAdapter.MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.memo_item_row, parent, false);
        return new MemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoAdapter.MemoViewHolder holder, int position) {
        MemoWithTags currMemo = memoList.get(position);
        holder.getTitle().setText(currMemo.voiceMemo.title);
        String folderName = currMemo.voiceMemo.folder.equals(DefaultFolders.ALL.name()) ?
                context.getResources().getString(DefaultFolders.ALL.getResID()) : currMemo.voiceMemo.folder;
        holder.getFolder().setText(folderName);
        Duration duration = currMemo.voiceMemo.duration;
        holder.getDuration().setText(DateUtils.formatElapsedTime(duration.getSeconds()));
        holder.getPlayButton().setOnClickListener(v -> {
            if (playListener != null) {
                if (position != RecyclerView.NO_POSITION & holder.getPlayButton().isChecked()) {
                    playListener.onPlayButtonClick(holder.getViewForeground(), position);
                }
            }
        });
        holder.getViewForeground().setOnClickListener(v -> {
            if (listener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(v, position);
                }
            }
        });
        Chip addChip = holder.getAddTagChip();
        addChip.setOnClickListener(v -> {
            if (listener != null) {
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(v, position);
                }
            }
        });
        holder.getChipGroup().removeAllViews();
        holder.getChipGroup().addView(addChip);
        List<Tag> tags = currMemo.tagList;
        for (Tag tag : tags) {
            Chip chip = new Chip(context);
            chip.setText(tag.tagName);
            holder.getChipGroup().addView(chip, 0);
        }
    }

    @Override
    public int getItemCount() {
        return memoList.size();
    }



    public void setMemoList(List<MemoWithTags> memoList) {
        this.memoList = memoList;
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MemoWithTags getItem(int position) {
        return memoList.get(position);
    }

    public void setPlayListener(OnPlayButtonClickListener playListener) {
        this.playListener = playListener;
    }

}
