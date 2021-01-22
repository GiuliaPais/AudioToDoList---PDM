package it.uninsubria.pdm.audiotodolist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.data.MemoWithTags;
import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.entity.VoiceMemo;
import it.uninsubria.pdm.audiotodolist.fragments.FolderListFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragmentDirections;

public class MainActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener, MemoListFragment.OnActionBarListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;

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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        boolean externalStorageWritable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (externalStorageWritable) {
            File folder = ContextCompat.getExternalFilesDirs(this, Environment.DIRECTORY_MUSIC)[0];
            if (folder != null) {
                folderRoot = folder;
            } else {
                folderRoot = ContextCompat.getExternalFilesDirs(this, null)[0];
            }
            return;
        }
        folderRoot = getFilesDir();
    }

    private void startRecording() {
        now = LocalDateTime.now();
        String filename = now.toString() + ".3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(folderRoot.getAbsolutePath() + "/" + filename);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
        File file = new File(folderRoot.getAbsolutePath() + "/" + lastFileName);
        VoiceMemo voiceMemo = new VoiceMemo();
        voiceMemo.path = file.getAbsolutePath();
        voiceMemo.dateTime = now;
        voiceMemo.folder = "ALL";
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

    }

    @Override
    public void onChangeActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }
}