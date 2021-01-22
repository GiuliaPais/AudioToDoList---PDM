package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.HashMap;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private final LayoutInflater inflater;
    private List<MemoWithTags> memoList;
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    /**
     * View holder for recycler view.
     */
    static class MemoViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private Button playButton, stopButton;
        private Slider slider;

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
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
        return 0;
    }

    public void setMemoList(List<MemoWithTags> memoList) {
        this.memoList = memoList;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
