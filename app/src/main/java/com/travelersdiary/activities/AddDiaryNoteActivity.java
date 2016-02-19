package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryFragment;

import butterknife.Bind;

public class AddDiaryNoteActivity extends BaseActivity {
    @Bind(R.id.add_diary_note_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary_note);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            supportActionBar.setTitle("Add new note");
        }

        if (savedInstanceState == null) {
            DiaryFragment diaryFragment = new DiaryFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("editing mode", true);
            diaryFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, diaryFragment)
                    .commit();
        }
    }
}
