package it.uninsubria.pdm.audiotodolist;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.fragments.FolderListFragmentDirections;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListAllNotesFragmentDirections;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragmentDirections;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener, MemoListFragment.OnActionBarListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private DrawerLayout drawer;
    private FrameLayout bottom_sheet;
    private Chronometer bottom_sheet_timer;
    private NavController navController;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SwitchMaterial nightModeSwitch;

    private MemoViewModel viewModel;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private File folderRoot;
    private String lastFileName;
    private LocalDateTime now;

    // Permissions management
    private boolean permissionRecordGranted = false;
    private String[] permissionsToGrant = {Manifest.permission.RECORD_AUDIO};

    private SharedPreferences preferences;
    private int currentUIMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(Context.MODE_PRIVATE);
        currentUIMode = preferences.getInt(getResources().getString(R.string.pref_night_mode), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer);
        bottom_sheet = findViewById(R.id.sheetContainer);
        bottom_sheet_timer = bottom_sheet.findViewById(R.id.elapsed);
        navController = Navigation.findNavController(this, R.id.memoList);
        navController.addOnDestinationChangedListener(this);
        navigationView = findViewById(R.id.navigation_view);
        nightModeSwitch = (SwitchMaterial) navigationView.getMenu().findItem(R.id.nightModeToggle).getActionView();
        setSupportActionBar(toolbar);
        // Show and Manage the Drawer and Back Icon
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.allMemoListFragment, R.id.memoListFragment)
                .setOpenableLayout(drawer)
                .build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navigationView.setNavigationItemSelectedListener(this);
        if (isUsingDarkMode()) {
            nightModeSwitch.setChecked(true);
        } else {
            nightModeSwitch.setChecked(false);
        }
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                currentUIMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                currentUIMode = AppCompatDelegate.MODE_NIGHT_NO;
            }
            preferences.edit().putInt(getResources().getString(R.string.pref_night_mode), currentUIMode).apply();
            AppCompatDelegate.setDefaultNightMode(currentUIMode);
        });
        permissionRecordGranted = checkPermission(Manifest.permission.RECORD_AUDIO);
        findFolderRoot();
        viewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        if (savedInstanceState == null) {
            navController.navigate(R.id.allMemoListFragment);
            getSupportActionBar().setTitle(R.string.all_notes);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawer);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionRecordGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    public void onBtnClick(View view) {
        if (!permissionRecordGranted) {
            ActivityCompat.requestPermissions(this, permissionsToGrant, REQUEST_RECORD_AUDIO_PERMISSION);
            if (!permissionRecordGranted) {
                return;
            }
        }
        if (!isRecording) {
            isRecording = true;
            bottom_sheet.setVisibility(View.VISIBLE);
            bottom_sheet_timer.setBase(SystemClock.elapsedRealtime());
            bottom_sheet_timer.start();
            startRecording();
        } else {
            isRecording = false;
            bottom_sheet.setVisibility(View.GONE);
            bottom_sheet_timer.stop();
            stopRecording();
        }
    }


    private boolean checkPermission(String permission) {
        if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void findFolderRoot() {
        File folder;
        boolean externalStorageWritable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (externalStorageWritable) {
            folder = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MUSIC)[0];
            if (folder != null) {
                folder = new File(folder, "/recordings");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                folderRoot = folder;
                return;
            }
        }
        folder = new File(getFilesDir(), "/recordings");
        if (!folder.exists()) {
            folder.mkdir();
        }
        folderRoot = folder;
    }

    private void startRecording() {
        now = LocalDateTime.now();
        String filename = now.toString() + ".m4a";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(folderRoot.getAbsolutePath() + "/" + filename);
        try {
            recorder.prepare();
            recorder.start();
            lastFileName = filename;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        String filePath = folderRoot.getAbsolutePath() + "/" + lastFileName;
        File file = new File(filePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String durationToParse = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Long duration = Long.parseLong(durationToParse);
        VoiceMemo voiceMemo = new VoiceMemo();
        //Setting default values for new voice memo
        voiceMemo.title = getResources().getString(R.string.default_new_memo_title) + now.toString();
        voiceMemo.path = filePath;
        voiceMemo.dateTime = now;
        voiceMemo.duration = Duration.ofMillis(duration);
        voiceMemo.folder = DefaultFolders.ALL.name();
        //Add to DB
        try {
            viewModel.createNewMemo(voiceMemo);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        MemoWithTags newMemo = new MemoWithTags();
        newMemo.voiceMemo = voiceMemo;
        if (navController.getCurrentDestination().getId() == R.id.allMemoListFragment) {
            MemoListAllNotesFragmentDirections.ActionAllMemoListFragmentToMemoDetailsFragment action =
                    MemoListAllNotesFragmentDirections.actionAllMemoListFragmentToMemoDetailsFragment(true, false, newMemo.voiceMemo.title);
            navController.navigate(action);
        } else if (navController.getCurrentDestination().getId() == R.id.memoListFragment) {
            MemoListFragmentDirections.MemoDetailsAction action = MemoListFragmentDirections.memoDetailsAction(true, false, newMemo.voiceMemo.title);
            navController.navigate(action);
        } else if (navController.getCurrentDestination().getId() == R.id.folderFragment) {
            FolderListFragmentDirections.ActionFolderFragmentToMemoDetailsFragment action = FolderListFragmentDirections.actionFolderFragmentToMemoDetailsFragment(true, false, newMemo.voiceMemo.title);
            navController.navigate(action);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        FloatingActionButton recordBtn = findViewById(R.id.recordButton);
        if (destination.getId() == R.id.memoDetailsFragment) {
            recordBtn.setVisibility(View.GONE);
        } else {
            recordBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChangeActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.allMemoListFragment :
                navController.navigate(R.id.allMemoListFragment);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            case R.id.folderFragment:
                navController.navigate(R.id.folderFragment);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            default:
                return false;
        }
    }

    private boolean isUsingDarkMode() {
        int currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            default:
                return false;
        }
    }

}