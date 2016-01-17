package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryActivity extends BaseActivity {

    @Bind(R.id.diary_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Diary title");
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new DiaryFragment())
                .commit();
    }
}
