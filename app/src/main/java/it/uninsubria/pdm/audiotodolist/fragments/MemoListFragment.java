package it.uninsubria.pdm.audiotodolist.fragments;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;

public class MemoListFragment extends Fragment implements MemoAdapter.OnItemClickListener, MemoRecyclerTouchHelper.MemoRecyclerTouchHelperListener {

    private MemoViewModel viewModel;
    private MemoAdapter adapter;
    private LiveData<List<MemoWithTags>> currentMemoList;
    private OnActionBarListener listener;

    public MemoListFragment() {
        super(R.layout.recycler_view_fragment);
    }

    public interface OnActionBarListener {
        void onChangeActionBarTitle(String title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        currentMemoList = viewModel.getVisibleMemos();
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
        currentMemoList.observe(getViewLifecycleOwner(), memos -> {
            if (memos != null) {
                adapter.setMemoList(memos);
                return;
            }
            adapter.setMemoList(new ArrayList<>());
        });
        RecyclerView recyclerView = view.findViewById(R.id.memo_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.SimpleCallback itemTouchHelper = new MemoRecyclerTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onItemClick(View itemView, int position) {
        MemoWithTags memoClicked = adapter.getItem(position);
        NavController controller = Navigation.findNavController(getActivity(), R.id.memoList);
        MemoListFragmentDirections.MemoDetailsAction action = MemoListFragmentDirections.memoDetailsAction(false, memoClicked);
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
                final String memoName = memoViewHolder.getTitle().getText().toString();
                final int itemPosition = memoViewHolder.getAdapterPosition();
                Context context = viewHolder.itemView.getContext();
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
                dialogBuilder.setTitle(R.string.delete_note)
                        .setMessage(R.string.delete_memo_confirm)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            viewModel.deleteMemo(memoName);
                            //delete file TODO
                        })
                        .setNegativeButton(R.string.no, (dialog, which) -> {
                            adapter.notifyItemChanged(itemPosition);
                        })
                        .create()
                        .show();
            }
        }
    }
}
