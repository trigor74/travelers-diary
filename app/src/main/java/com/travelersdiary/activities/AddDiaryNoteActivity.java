package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddDiaryNoteActivity extends BaseActivity {
    @Bind(R.id.add_diary_note_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary_note);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            supportActionBar.setTitle("Add new note");
        }

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, new DiaryEditorFragment())
//                    .commit();
//        }
    }
}
