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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.entity.Folder;
import it.uninsubria.pdm.audiotodolist.fragments.FolderListFragment;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawer;
    private MemoViewModel viewModel;
    private MediaRecorder recorder;
    private boolean isRecording = false;
    private File folderRoot;
    private String lastFileName;

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
        drawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        permissionRecordGranted = checkPermission(Manifest.permission.RECORD_AUDIO);
        findFolderRoot();
        viewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        if (savedInstanceState == null) {
           navigationView.setCheckedItem(R.id.menu_item_all_notes);
           loadMemoListFragment();
        }
    }



    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
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

    private void loadFolderFragment() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.memoList, FolderListFragment.class, null)
                .commit();
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
            File folder = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (folder != null) {
                folderRoot = folder;
            } else {
                folderRoot = getExternalFilesDir(null);
            }
            return;
        }
        folderRoot = getFilesDir();
    }

    private void startRecording() {
        LocalDateTime now = LocalDateTime.now();
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
        recorder.release();
        recorder = null;
        //launch intent
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_user_folders :
                loadFolderFragment();
                break;
            case R.id.menu_item_all_notes :
                loadMemoListFragment();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}