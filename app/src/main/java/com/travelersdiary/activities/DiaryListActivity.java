package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryListFragment;

import butterknife.Bind;
import butterknife.OnClick;

public class DiaryListActivity extends BaseActivity {
    private static final String DIARY_LIST_FRAGMENT_TAG = "DIARY_LIST_FRAGMENT_TAG";

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @OnClick(R.id.main_activity_fab)
    public void onFabClick() {
        Intent intent = new Intent(this, DiaryActivity.class);
        intent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            DiaryListFragment fragment = new DiaryListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, DIARY_LIST_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setCheckedItem(R.id.nav_diary);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Exit me", true);
        startActivity(intent);
        finish();
    }

    @Override
    protected boolean useDrawerToggle() {
        return true;
    }
}
