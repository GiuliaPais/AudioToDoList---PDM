package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
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
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.dialogs.FolderDialogFragment;
import it.uninsubria.pdm.audiotodolist.entity.Folder;

public class FolderListFragment extends Fragment implements FolderAdapter.OnItemClickListener, FolderRecycleTouchHelper.FolderRecycleTouchHelperListener {
    private MemoViewModel viewModel;
    private LiveData<List<Folder>> folderList;
    private FolderAdapter adapter;
    private FolderDialogFragment.FolderDialogFragmentListener dialogListener;

    public FolderListFragment() {
        super(R.layout.folders_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        folderList = viewModel.getAllFolders();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.folder_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder :
                showNewFolderDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new FolderAdapter(getContext());
        adapter.setFolderList(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        folderList.observe(getViewLifecycleOwner(), folders -> {
            if (folders != null) {
                adapter.setFolderList(folders);
                return;
            }
            adapter.setFolderList(new ArrayList<>());
        });
        RecyclerView recyclerView = view.findViewById(R.id.folderRecycleView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemTouchHelper.SimpleCallback itemTouchHelper = new FolderRecycleTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(recyclerView);
    }

    private void showNewFolderDialog() {
        NavController controller = Navigation.findNavController(getActivity(), R.id.memoList);
        if (dialogListener == null) {
            dialogListener = new FolderDialogFragment.FolderDialogFragmentListener() {
                @Override
                public void onDialogPositiveClick(DialogFragment dialog) {
                    EditText text = dialog.getDialog().findViewById(R.id.folderNameEditText);
                    String folderName = text.getText().toString();
                    if (folderName != null & !folderName.isEmpty()) {
                        Folder newFolder = new Folder(folderName);
                        viewModel.createNewFolder(newFolder);
                    }
                }
                @Override
                public void onDialogNegativeClick(DialogFragment dialog) {

                }
            };
        }
        FolderListFragmentDirections.ActionFolderFragmentToFolderDialogFragment action = FolderListFragmentDirections.actionFolderFragmentToFolderDialogFragment(dialogListener);
        controller.navigate(action);
    }


    @Override
    public void onItemClick(View itemView, int position) {
        Log.i("FOLDER FRAGMENT", "Item clicked!");
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (direction == 4) {
            FolderAdapter.FolderViewHolder folderViewHolder = null;
            if (viewHolder instanceof FolderAdapter.FolderViewHolder) {
                folderViewHolder = (FolderAdapter.FolderViewHolder) viewHolder;
            }
            if (folderViewHolder != null) {
                final String folderName = folderViewHolder.getTextView().getText().toString();
                final int itemPosition = folderViewHolder.getAdapterPosition();
                Context context = viewHolder.itemView.getContext();
                MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
                dialogBuilder.setTitle(R.string.delete_folder)
                        .setMessage(R.string.delete_confirm_msg)
                        .setPositiveButton(R.string.yes, (dialog, which) -> {
                            viewModel.deleteFolder(folderName);
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
