package it.uninsubria.pdm.audiotodolist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import it.uninsubria.pdm.audiotodolist.database.MemoViewModel;
import it.uninsubria.pdm.audiotodolist.fragments.MemoListFragment;

public class MainActivity extends AppCompatActivity {

    private MemoViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewModel = new ViewModelProvider(this).get(MemoViewModel.class);
        if (savedInstanceState == null) {
           getSupportFragmentManager().beginTransaction()
                   .setReorderingAllowed(true)
                   .add(R.id.memoList, MemoListFragment.class, null)
                   .commit();
        }
    }
}