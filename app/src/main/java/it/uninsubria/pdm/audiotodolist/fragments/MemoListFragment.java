package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.MediaPlayerManager;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.Tag;

public class MemoListFragment extends Fragment implements MemoAdapter.OnItemClickListener,
        MemoRecyclerTouchHelper.MemoRecyclerTouchHelperListener, MemoAdapter.OnPlayButtonClickListener {

    private static final String FILE_PROVIDER = "it.uninsubria.pdm.audiotodolist.fileprovider";
    protected MemoViewModel viewModel;
    protected MemoAdapter adapter;
    protected LiveData<List<MemoWithTags>> currentMemoList;
    protected LiveData<List<Tag>> allTagsObs;
    protected OnActionBarListener listener;
    protected ChipGroup filterGroup;
    protected int previouslyPlayed = -1;

    public MemoListFragment() {
        super(R.layout.recycler_view_fragment);
    }

    public interface OnActionBarListener {
        void onChangeActionBarTitle(String title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        currentMemoList = viewModel.getVisibleMemos();
        allTagsObs = viewModel.getAllTags();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            listener = (OnActionBarListener) context;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getSelectedFolder().observe(getViewLifecycleOwner(), s -> {
            if (s != null) {
                if (s.equals(DefaultFolders.ALL.name())) {
                    listener.onChangeActionBarTitle(getResources().getString(DefaultFolders.ALL.getResID()));
                } else {
                    listener.onChangeActionBarTitle(s);
                }
            }
        });
        adapter = new MemoAdapter(getContext());
        adapter.setListener(this);
        adapter.setPlayListener(this);
        currentMemoList.observe(getViewLifecycleOwner(), memos -> {
            if (memos != null) {
                adapter.setMemoList(memos);
                return;
            }
            adapter.setMemoList(new ArrayList<>());
        });
        filterGroup = view.findViewById(R.id.horizontalScrollView).findViewById(R.id.filter_chip_group);
        RecyclerView recyclerView = view.findViewById(R.id.memo_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.SimpleCallback itemTouchHelper = new MemoRecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);
        allTagsObs.observe(getViewLifecycleOwner(), tags -> {
            List<String> tagsAsStrings = tags.stream().map(tag -> tag.tagName).collect(Collectors.toList());
            initTagChips(tagsAsStrings);
        });
    }

    @Override
    public void onItemClick(View itemView, int position) {
        MemoWithTags memoClicked = adapter.getItem(position);
        NavController controller = Navigation.findNavController(requireActivity(), R.id.memoList);
        if (itemView.getId() == R.id.addTagChip) {
            MemoListFragmentDirections.MemoDetailsAction action = MemoListFragmentDirections.memoDetailsAction(false, true, memoClicked.voiceMemo.title);
            controller.navigate(action);
            return;
        }
        MemoListFragmentDirections.MemoDetailsAction action = MemoListFragmentDirections.memoDetailsAction(false, false, memoClicked.voiceMemo.title);
        controller.navigate(action);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (direction == 4) {
            MemoAdapter.MemoViewHolder memoViewHolder = null;
            if (viewHolder instanceof MemoAdapter.MemoViewHolder) {
                memoViewHolder = (MemoAdapter.MemoViewHolder) viewHolder;
            }
            if (memoViewHolder != null) {
                final int itemPosition = memoViewHolder.getAdapterPosition();
                MemoWithTags memo = adapter.getItem(itemPosition);
                Context context = viewHolder.itemView.getContext();
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
                dialogBuilder.setTitle(R.string.delete_note)
                        .setMessage(R.string.delete_memo_confirm)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            deleteFile(true, memo);
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> {
                            adapter.notifyItemChanged(itemPosition);
                        })
                        .create()
                        .show();
            }
        }
    }

    @Override
    public void onPlayButtonClick(View itemView, int position) {
        if (position == previouslyPlayed) {
            return;
        }
        previouslyPlayed = position;
        MemoWithTags selectedMemo = adapter.getItem(position);
        File file = new File(selectedMemo.voiceMemo.path);
        if (!file.exists()) {
            missingFileDialog(selectedMemo);
            return;
        }
        Uri uri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER, file);
        FrameLayout playerContainer = itemView.findViewById(R.id.playerContainer);
        MaterialButton play = playerContainer.findViewById(R.id.play);
        MaterialButton stop = playerContainer.findViewById(R.id.stop);
        Slider slider = playerContainer.findViewById(R.id.slider);
        TextView elapsed = playerContainer.findViewById(R.id.durationTrack1);
        MediaPlayerManager.attachAndPlay(requireContext(), play, stop, slider, elapsed, uri);
    }

    private void missingFileDialog(MemoWithTags memo) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setTitle(R.string.file_not_found)
                .setMessage(R.string.file_not_found_body)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteFile(false, memo);
                    Toast.makeText(requireContext(), R.string.file_deleted_toast, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void deleteFile(boolean fileInStorage, MemoWithTags memo) {
        viewModel.deleteMemo(memo.voiceMemo.title);
        if (fileInStorage) {
            File file = new File(memo.voiceMemo.path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    protected void initTagChips(List<String> tags) {
        filterGroup.removeAllViews();
        viewModel.clearSelectedTags();
        for (String tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    viewModel.addToTagSelection(buttonView.getText().toString());
                } else {
                    viewModel.removeTagFromSelection(buttonView.getText().toString());
                }
            });
            filterGroup.addView(chip);
        }
    }

}
