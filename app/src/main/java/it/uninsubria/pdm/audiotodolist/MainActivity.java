package it.uninsubria.pdm.audiotodolist;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

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
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import it.uninsubria.pdm.audiotodolist.data.DefaultFolders;
import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.fragments.MemoDetailsFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoDetailsFragmentDirections;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragmentDirections;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener, MemoListFragment.OnActionBarListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private MemoViewModel viewModel;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private File folderRoot;
    private String lastFileName;
    private LocalDateTime now;

    // Permissions management
    private boolean permissionRecordGranted = false;
    private String[] permissionsToGrant = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer);
        navController = Navigation.findNavController(this, R.id.memoList);
        navController.addOnDestinationChangedListener(this);
        navigationView = findViewById(R.id.navigation_view);

        // Show and Manage the Drawer and Back Icon
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);

        // Handle Navigation item clicks
        NavigationUI.setupWithNavController(navigationView, navController);
        permissionRecordGranted = checkPermission(Manifest.permission.RECORD_AUDIO);
        findFolderRoot();
        viewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        if (savedInstanceState == null) {
           navigationView.setCheckedItem(R.id.memoListFragment);
           loadMemoListFragment();
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
            FloatingActionButton recordBtn = findViewById(R.id.recordButton);
            startRecording();
        } else {
            isRecording = false;
            stopRecording();
        }
    }

    private void loadMemoListFragment() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.memoList, MemoListFragment.class, null)
                .commit();
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
                Log.i("MAIN", "Folder path (external): " + folder.getAbsolutePath());
                folderRoot = folder;
                return;
            }
        }
        folder = new File(getFilesDir(), "/recordings");
        if (!folder.exists()) {
            folder.mkdir();
        }
        Log.i("MAIN", "Folder path (internal): " + folder.getAbsolutePath());
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
            //inserire dialog?
        }
    }

    private void stopRecording() {
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
        String filePath = folderRoot.getAbsolutePath() + "/" + lastFileName;
        File file = new File(filePath);
        if (!file.exists()) {
            //launch error
            return;
        }
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
        MemoListFragmentDirections.MemoDetailsAction action = MemoListFragmentDirections.memoDetailsAction(true, newMemo);
        navController.navigate(action);
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

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}