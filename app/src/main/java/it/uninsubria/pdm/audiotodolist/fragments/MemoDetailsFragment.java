package it.uninsubria.pdm.audiotodolist.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import java.util.Objects;
import java.util.stream.Collectors;

import it.uninsubria.pdm.audiotodolist.MainActivity;
import it.uninsubria.pdm.audiotodolist.MediaPlayerManager;
import it.uninsubria.pdm.audiotodolist.R;
import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.dialogs.TagsDialogFragment;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.Tag;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;

public class MemoDetailsFragment extends Fragment {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);
    private static final String FILE_PROVIDER = "it.uninsubria.pdm.audiotodolist.fileprovider";
    private Uri fileUri;

    private MemoViewModel viewModel;
    private LiveData<MemoWithTags> memo;

    private EditText titleEditText;
    private TextView timestamp, durationTrack2, durationTrack1;
    private Spinner folderSpinner;
    private TextInputEditText comments;
    private Slider slider;
    private MaterialButton play, stop;
    private ChipGroup tagGroup;
    private Chip addChip;
    protected TagsDialogFragment.TagsDialogFragmentListener tagDialogListener;

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
                    viewModel.registerMemoChanges(memo.getValue(), newMemo);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            refresh(Objects.requireNonNull(memo.getValue()));
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
        assert getArguments() != null;
        boolean editModeOn = MemoDetailsFragmentArgs.fromBundle(getArguments()).getIsEditMode();
        boolean tagDialogOpen = MemoDetailsFragmentArgs.fromBundle(getArguments()).getIsTagDialogOpen();
        String memoTitle = MemoDetailsFragmentArgs.fromBundle(getArguments()).getMemoTitle();
        viewModel.setMemoTitle(memoTitle);
        initRefs();
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
        addChip.setOnClickListener(v -> {
            tagDialog();
        });
        memo = viewModel.getCurrentMemo();
        memo.observe(getViewLifecycleOwner(), memoWithTags -> {
            if (memoWithTags != null) {
                refresh(memoWithTags);
            }
        });
        folderSpinner.setAdapter(adapter);
        if (editModeOn) {
            ((MainActivity) requireActivity()).startSupportActionMode(editActionMode);
        } else {
            folderSpinner.setEnabled(false);
        }
        if (tagDialogOpen) {
            tagDialog();
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
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle(R.string.details);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.memo_detail_edit:
                ((MainActivity) requireActivity()).startSupportActionMode(editActionMode);
                return true;
            case R.id.memo_detail_delete:
                deleteMemoDialog();
                return true;
            case R.id.memo_detail_share:
                if (fileUri == null) {
                    missingFileDialog();
                    return false;
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.putExtra(Intent.EXTRA_STREAM, fileUri);
                share.setType("audio/m4a");
                Intent chooser = Intent.createChooser(share, getResources().getString(R.string.share));
                List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    requireActivity().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(chooser);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh(MemoWithTags memoWithTags) {
        titleEditText.setText(memoWithTags.voiceMemo.title);
        timestamp.setText(memoWithTags.voiceMemo.dateTime.format(dateTimeFormatter));
        if (memoWithTags.voiceMemo.comment != null) {
            comments.setText(memoWithTags.voiceMemo.comment);
        }
        String folderName = memoWithTags.voiceMemo.folder.equals(DefaultFolders.ALL.name()) ? getResources().getString(DefaultFolders.ALL.getResID()) : memoWithTags.voiceMemo.folder;
        int indexInList = adapter.getPosition(folderName);
        folderSpinner.setSelection(indexInList);
        String formattedDuration = DateUtils.formatElapsedTime(memoWithTags.voiceMemo.duration.getSeconds());
        durationTrack2.setText(formattedDuration);
        tagGroup.removeAllViews();
        tagGroup.addView(addChip);
        for (Tag t : memoWithTags.tagList) {
            Chip c = new Chip(requireContext());
            c.setText(t.tagName);
            c.setCloseIconVisible(true);
            c.setOnCloseIconClickListener(v -> {
                Chip chip = (Chip) v;
                Tag tag = new Tag();
                tag.tagName = chip.getText().toString();
                viewModel.deleteTagsFromMemo(memo.getValue(), tag);
            });
            tagGroup.addView(c, 0);
        }
        File file = new File(memoWithTags.voiceMemo.path);
        if (!file.exists()) {
            missingFileDialog();
        } else {
            fileUri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER, file);
            //For media player (only if file exists)
            MediaPlayerManager.attach(getContext(), play, stop, slider, durationTrack1, fileUri);
        }
    }

    private MemoWithTags catchChanges() {
        MemoWithTags currMemo = memo.getValue();
        VoiceMemo modVoiceMemo = new VoiceMemo();
        modVoiceMemo.title = titleEditText.getText().toString();
        modVoiceMemo.duration = currMemo.voiceMemo.duration;
        String folderName = folderSpinner.getSelectedItem().toString().equals(getResources().
                getString(DefaultFolders.ALL.getResID())) ? DefaultFolders.ALL.name() : folderSpinner.getSelectedItem().toString();
        modVoiceMemo.folder = folderName;
        modVoiceMemo.path = currMemo.voiceMemo.path;
        modVoiceMemo.dateTime = currMemo.voiceMemo.dateTime;
        modVoiceMemo.comment = comments.getText().toString();
        MemoWithTags newMemoWithTags = new MemoWithTags();
        newMemoWithTags.voiceMemo = modVoiceMemo;
        newMemoWithTags.tagList = currMemo.tagList;
        return newMemoWithTags;
    }

    private void initRefs() {
        titleEditText = getActivity().findViewById(R.id.memoTitleTextView);
        timestamp = getActivity().findViewById(R.id.dateRegistered);
        folderSpinner = getActivity().findViewById(R.id.folderSpinner);
        comments = getActivity().findViewById(R.id.memoComments);
        FrameLayout framePlayer = getActivity().findViewById(R.id.framePlayerMemoFragment);
        durationTrack2 = framePlayer.findViewById(R.id.durationTrack2);
        durationTrack1 = framePlayer.findViewById(R.id.durationTrack1);
        slider = framePlayer.findViewById(R.id.slider);
        play = framePlayer.findViewById(R.id.play);
        stop = framePlayer.findViewById(R.id.stop);
        tagGroup = getActivity().findViewById(R.id.tagGroupMemo);
        addChip = getActivity().findViewById(R.id.addTagChipMemo);
    }

    private void deleteFile(boolean fileInStorage) {
        viewModel.deleteMemo(Objects.requireNonNull(memo.getValue()).voiceMemo.title);
        if (fileInStorage) {
            File file = new File(memo.getValue().voiceMemo.path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void missingFileDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setTitle(R.string.file_not_found)
                .setMessage(R.string.file_not_found_body)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteFile(false);
                    Navigation.findNavController(requireActivity(), R.id.memoList).popBackStack();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void deleteMemoDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setTitle(R.string.delete_note)
                .setMessage(R.string.delete_memo_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteFile(true);
                    Navigation.findNavController(requireActivity(), R.id.memoList).popBackStack();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .create()
                .show();
    }

    protected void initTagDialogListener() {
        if (tagDialogListener == null) {
            tagDialogListener = new TagsDialogFragment.TagsDialogFragmentListener() {
                @Override
                public void onDialogPositiveClick(DialogFragment dialog) {
                    EditText editText = dialog.getDialog().findViewById(R.id.tagEditText);
                    String tags = editText.getText().toString();
                    if (tags.isEmpty()) {
                        return;
                    }
                    String[] splitted = tags.trim().split("\\s*,\\s*");
                    List<Tag> tagList = new ArrayList<>();
                    for (String s : splitted) {
                        Tag t = new Tag();
                        t.tagName = s;
                        tagList.add(t);
                    }
                    viewModel.addTagsToMemo(memo.getValue(), tagList.toArray(new Tag[0]));
                }

                @Override
                public void onDialogNegativeClick(DialogFragment dialog) {
                }
            };
        }
    }

    protected void tagDialog() {
        initTagDialogListener();
        MemoDetailsFragmentDirections.ActionMemoDetailsFragmentToTagsDialogFragment action = MemoDetailsFragmentDirections.actionMemoDetailsFragmentToTagsDialogFragment(tagDialogListener);
        Navigation.findNavController(requireActivity(), R.id.memoList).navigate(action);
    }
}
