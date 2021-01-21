package it.uninsubria.pdm.audiotodolist.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.Folder;

public class FolderListFragment extends Fragment {
    private MemoViewModel viewModel;

    public FolderListFragment() {
        super(R.layout.folders_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(R.string.folder_fragment_toolbar_title);
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
                Log.i("FRAGMENT", "New folder clicked");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        FolderAdapter adapter = new FolderAdapter(getContext());
        LiveData<List<Folder>> folderList = viewModel.getAllFolders();
        if (folderList != null & folderList.getValue() != null) {
            adapter.setFolderList(folderList.getValue());
        } else {
            adapter.setFolderList(new ArrayList<>());
        }
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
    }
}
