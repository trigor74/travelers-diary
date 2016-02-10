package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryFragment;

import butterknife.Bind;

public class DiaryActivity extends BaseActivity {

    @Bind(R.id.diary_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Diary title");
        }

        if (savedInstanceState == null) {
            String key = getIntent().getStringExtra(Constants.KEY_DAIRY_NOTE_REF);

            DiaryFragment diaryFragment = new DiaryFragment();

            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_DAIRY_NOTE_REF, key);
            diaryFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, diaryFragment)
                    .commit();
        }
    }

}
