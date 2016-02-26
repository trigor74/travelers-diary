package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.travelersdiary.R;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.dialogs.EditTravelDialog;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    public static final String KEY_TAB_POSITION = "TAB_POSITION";

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.view_pager)
    ViewPager mViewPager;

    @OnClick(R.id.main_activity_fab)
    public void onClickFAB(View v) {
        switch (mViewPager.getCurrentItem()) {
            case 0: // Travels Tab
                FragmentManager fragmentManager = getSupportFragmentManager();
                EditTravelDialog addTravelDialog = new EditTravelDialog();
                addTravelDialog.show(fragmentManager, "dialog");
                break;
            case 1: // Diary Tab
                Intent diaryIntent = new Intent(this, AddDiaryNoteActivity.class);
                //diaryIntent.putExtra(key, value);
                startActivity(diaryIntent);
                break;
            case 2: // Reminder Tab
                Intent remindItemIntent = new Intent(this, RemindItemActivity.class);
                startActivity(remindItemIntent);
                break;
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupViewPager();

        openSelectedTab();
    }

    public void setupViewPager() {
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.travels_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.diary_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.reminder_text));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (getSupportFragmentManager(), MainActivity.this, mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                mNavigationView.getMenu().getItem(tab.getPosition()).setChecked(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_travels:
                mTabLayout.getTabAt(0).select();
                break;
            case R.id.nav_diary:
                mTabLayout.getTabAt(1).select();
                break;
            case R.id.nav_reminder:
                mTabLayout.getTabAt(2).select();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean useDrawerToggle() {
        return true;
    }

    private void openSelectedTab() {
        Intent intent = getIntent();
        int tabToOpen = intent.getIntExtra(KEY_TAB_POSITION, 0);

        mViewPager.setCurrentItem(tabToOpen);
        mNavigationView.getMenu().getItem(tabToOpen).setChecked(true);
    }

    private boolean isTabletLandMode() {
        return getResources().getBoolean(R.bool.isTabletLand);
    }
}
