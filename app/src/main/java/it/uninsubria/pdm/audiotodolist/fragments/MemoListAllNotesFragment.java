package it.uninsubria.pdm.audiotodolist.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;

public class MemoListAllNotesFragment extends MemoListFragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        super.viewModel.setSelectedFolder(DefaultFolders.ALL.name());
    }

    @Override
    public void onItemClick(View itemView, int position) {
        MemoWithTags memoClicked = adapter.getItem(position);
        NavController controller = Navigation.findNavController(requireActivity(), R.id.memoList);
        if (itemView.getId() == R.id.addTagChip) {
         MemoListAllNotesFragmentDirections.ActionAllMemoListFragmentToMemoDetailsFragment action =
                MemoListAllNotesFragmentDirections.actionAllMemoListFragmentToMemoDetailsFragment(false, true, memoClicked.voiceMemo.title);
        controller.navigate(action);
            return;
        }
        MemoListAllNotesFragmentDirections.ActionAllMemoListFragmentToMemoDetailsFragment action =
                MemoListAllNotesFragmentDirections.actionAllMemoListFragmentToMemoDetailsFragment(false, false, memoClicked.voiceMemo.title);
        controller.navigate(action);
    }

}
