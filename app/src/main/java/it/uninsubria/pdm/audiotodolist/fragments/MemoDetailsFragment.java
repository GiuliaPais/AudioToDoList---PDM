package it.uninsubria.pdm.audiotodolist.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;

public class MemoDetailsFragment extends Fragment {
    private MemoViewModel viewModel;
    private MemoWithTags memo;
    private ActionBar actionBar;

    private EditText titleEditText;
    private TextView timestamp;

    private ActionMode actionMode = null;

    private ActionMode.Callback editActionMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_memo_details_editmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            titleEditText.setEnabled(true);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.memo_detail_confirm:
                    //updatedb
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            titleEditText.setEnabled(false);
            actionMode = null;
        }
    };

    public MemoDetailsFragment() {
        super(R.layout.memo_details_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleEditText = getActivity().findViewById(R.id.memoTitleTextView);
        timestamp = getActivity().findViewById(R.id.dateRegistered);
        boolean editModeOn = MemoDetailsFragmentArgs.fromBundle(getArguments()).getIsEditMode();
        memo = MemoDetailsFragmentArgs.fromBundle(getArguments()).getMemo();
        if (memo == null) {
            Log.e("DETAILS", "Memo is null!");
            return;
        }
        if (editModeOn) {
            timestamp.setText(memo.voiceMemo.dateTime.toString());
            getActivity().startActionMode(editActionMode);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_memo_details, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }
}
