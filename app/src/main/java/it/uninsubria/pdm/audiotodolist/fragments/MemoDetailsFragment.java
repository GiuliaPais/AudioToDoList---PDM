package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.MediaPlayerManager;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoDetailsFragment extends Fragment {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
    private static final String FILE_PROVIDER = "it.uninsubria.pdm.audiotodolist.fileprovider";
    private Uri fileUri;

    private MemoViewModel viewModel;
    private MemoWithTags memo;
    private Toolbar toolbar;

    private EditText titleEditText;
    private TextView timestamp, durationTrack;
    private Spinner folderSpinner;
    private TextInputEditText comments;
    private Slider slider;

    //For spinner
    private LiveData<List<Folder>> folders;
    private List<String> folderNames;
    private ArrayAdapter<String> adapter;

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
            folderSpinner.setEnabled(true);
            comments.setEnabled(true);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.memo_detail_confirm:
                    MemoWithTags newMemo = catchChanges();
                    viewModel.registerMemoChanges(memo, newMemo);
                    memo = newMemo;
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            titleEditText.setEnabled(false);
            folderSpinner.setEnabled(false);
            comments.setEnabled(false);
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
        viewModel = new ViewModelProvider(requireActivity()).get(MemoViewModel.class);
        folders = viewModel.getAllFolders();
        folderNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, folderNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        folders.observe(getViewLifecycleOwner(), fol -> {
           if (fol != null) {
               List<String> names = fol.stream()
                       .map(f ->  f.folderName)
                       .collect(Collectors.toList());
               names.add(0, getResources().getString(DefaultFolders.ALL.getResID()));
               folderNames.clear();
               folderNames.addAll(names);
               adapter.notifyDataSetChanged();
           }
        });
        toolbar = ((MainActivity)getActivity()).getToolbar();
        titleEditText = getActivity().findViewById(R.id.memoTitleTextView);
        timestamp = getActivity().findViewById(R.id.dateRegistered);
        folderSpinner = getActivity().findViewById(R.id.folderSpinner);
        comments = getActivity().findViewById(R.id.memoComments);
        durationTrack = getActivity().findViewById(R.id.framePlayerMemoFragment).findViewById(R.id.durationTrack2);
        slider = getActivity().findViewById(R.id.framePlayerMemoFragment).findViewById(R.id.slider);

        folderSpinner.setAdapter(adapter);
        boolean editModeOn = MemoDetailsFragmentArgs.fromBundle(getArguments()).getIsEditMode();
        memo = MemoDetailsFragmentArgs.fromBundle(getArguments()).getMemo();
        titleEditText.setText(memo.voiceMemo.title);
        timestamp.setText(memo.voiceMemo.dateTime.format(dateTimeFormatter));
        if (memo.voiceMemo.comment != null) {
            comments.setText(memo.voiceMemo.comment);
        }
        String folderName = memo.voiceMemo.folder.equals(DefaultFolders.ALL.name()) ? getResources().getString(DefaultFolders.ALL.getResID()) : memo.voiceMemo.folder;
        int indexInList = adapter.getPosition(folderName);
        folderSpinner.setSelection(indexInList);
        String formattedDuration = DateUtils.formatElapsedTime(memo.voiceMemo.duration.getSeconds());
        durationTrack.setText(formattedDuration);
        //inittags
        if (editModeOn) {
            ((MainActivity) getActivity()).startSupportActionMode(editActionMode);
        } else {
            folderSpinner.setEnabled(false);
        }
        File file = new File(memo.voiceMemo.path);
        if (!file.exists()) {
            //Alert
        } else {
            fileUri = FileProvider.getUriForFile(getContext(), FILE_PROVIDER, file);
        }
        MaterialButton playBtn = getActivity().findViewById(R.id.framePlayerMemoFragment).findViewById(R.id.play);
        MaterialButton stopBtn = getActivity().findViewById(R.id.framePlayerMemoFragment).findViewById(R.id.stop);
        //For media player
        MediaPlayerManager.attach(getContext(), playBtn, stopBtn, slider);
        try {
            MediaPlayerManager.setFile(fileUri);
        } catch (IOException e) {
            e.printStackTrace();
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
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.details);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.memo_detail_edit:
                ((MainActivity) getActivity()).startSupportActionMode(editActionMode);
                return true;
            case R.id.memo_detail_delete:
                //TODO
                return true;
            case R.id.memo_detail_share:
                if (fileUri == null) {
                    //alert
                    return false;
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                share.setType("audio/m4a");
                startActivity(Intent.createChooser(share, getResources().getString(R.string.share)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private MemoWithTags catchChanges() {
        VoiceMemo modVoiceMemo = new VoiceMemo();
        modVoiceMemo.title = titleEditText.getText().toString();
        modVoiceMemo.duration = memo.voiceMemo.duration;
        String folderName = folderSpinner.getSelectedItem().toString().equals(getResources().
                getString(DefaultFolders.ALL.getResID())) ? DefaultFolders.ALL.name() : folderSpinner.getSelectedItem().toString();
        modVoiceMemo.folder = folderName;
        modVoiceMemo.path = memo.voiceMemo.path;
        modVoiceMemo.dateTime = memo.voiceMemo.dateTime;
        modVoiceMemo.comment = comments.getText().toString();
        //Chip management

        MemoWithTags newMemoWithTags = new MemoWithTags();
        newMemoWithTags.voiceMemo = modVoiceMemo;
        //tags

        return newMemoWithTags;
    }

}
