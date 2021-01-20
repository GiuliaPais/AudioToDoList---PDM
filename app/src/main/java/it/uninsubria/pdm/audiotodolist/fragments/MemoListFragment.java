package it.uninsubria.pdm.audiotodolist.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;

public class MemoListFragment extends Fragment {

    private MemoViewModel viewModel;

    public MemoListFragment() {
        super(R.layout.recycler_view_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        //observe current memo list
    }
}
