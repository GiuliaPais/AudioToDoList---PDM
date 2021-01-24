package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private final LayoutInflater inflater;
    private List<MemoWithTags> memoList = new ArrayList<>();
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    /**
     * View holder for recycler view.
     */
    static class MemoViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, duration, currentTime;
        private final Button playButton, stopButton;
        private final Slider slider;
        private final ConstraintLayout viewBackground, viewForeground;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.memoTitle);
            playButton = itemView.findViewById(R.id.play);
            stopButton = itemView.findViewById(R.id.stop);
            slider = itemView.findViewById(R.id.slider);
            duration = itemView.findViewById(R.id.durationTrack2);
            currentTime = itemView.findViewById(R.id.durationTrack1);
            viewBackground = itemView.findViewById(R.id.memo_item_background);
            viewForeground = itemView.findViewById(R.id.memo_item_foreground);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(itemView, position);
                    }
                }
            });
        }

        public TextView getTitle() {
            return title;
        }

        public Button getPlayButton() {
            return playButton;
        }

        public Button getStopButton() {
            return stopButton;
        }

        public Slider getSlider() {
            return slider;
        }

        public ConstraintLayout getViewBackground() {
            return viewBackground;
        }

        public ConstraintLayout getViewForeground() {
            return viewForeground;
        }
    }

    public MemoAdapter(Context context) {
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
        holder.getTitle().setText(memoList.get(position).voiceMemo.title);
        //init duration ecc
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
}
