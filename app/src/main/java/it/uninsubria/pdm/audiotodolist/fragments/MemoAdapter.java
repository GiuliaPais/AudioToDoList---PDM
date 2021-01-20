package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.MemoViewHolder> {

    private final LayoutInflater inflater;
    private List<VoiceMemo> voiceMemoList;
    private HashMap<String, List<String>> tags;

    /**
     * View holder for recycler view.
     */
    static class MemoViewHolder extends RecyclerView.ViewHolder {

        public MemoViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    MemoAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MemoAdapter.MemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MemoAdapter.MemoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

}
