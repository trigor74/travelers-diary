package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.interfaces.IActionModeFinishCallback;

import butterknife.Bind;
import butterknife.OnClick;

public class TravelActivity extends BaseActivity {

    @Bind(R.id.travel_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.travel_tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.travel_view_pager)
    ViewPager mViewPager;

    @Bind(R.id.travel_activity_fab)
    FloatingActionButton mTravelActivityFab;

    public static final String NEW_TRAVEL = "new travel";

    private String mTravelTitle;
    private String mTravelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        mTravelTitle = getIntent().getStringExtra(Constants.KEY_TRAVEL_TITLE);
        mTravelId = getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(mTravelTitle);
        }

        setupViewPager();
    }

    public void setupViewPager() {
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.diary_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.reminder_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.map_text));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (getSupportFragmentManager(), TravelActivity.this, mTabLayout.getTabCount());

        adapter.addFragment(new DiaryListFragment());
        adapter.addFragment(new ReminderListFragment());
        adapter.addFragment(new MapFragment());

        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 2) {
                    mTravelActivityFab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() < 2) {
                    finishActionMode(tab.getPosition());
                }

                if (tab.getPosition() == 2) {
                    mTravelActivityFab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void finishActionMode(int tabPosition) {
        Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getItem(tabPosition);
        try {
            IActionModeFinishCallback actionModeFinishCallback = (IActionModeFinishCallback) fragment;
            actionModeFinishCallback.finishActionMode();
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement IActionModeFinishCallback");
        }
    }

    @Override
    protected void onDrawerOpened(View drawerView) {
        finishActionMode(mTabLayout.getSelectedTabPosition());
        super.onDrawerOpened(drawerView);
    }

    @OnClick(R.id.travel_activity_fab)
    public void onFabClick() {
        switch (mViewPager.getCurrentItem()) {
            case 0: // Diary Tab
                Intent diaryIntent = new Intent(this, DiaryActivity.class);
                diaryIntent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
                diaryIntent.putExtra(Constants.KEY_TRAVEL_TITLE, mTravelTitle);
                diaryIntent.putExtra(Constants.KEY_TRAVEL_REF, mTravelId);
                startActivity(diaryIntent);
                break;
            case 1: // Reminder Tab
                Intent remindItemIntent = new Intent(this, ReminderItemActivity.class);
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_TITLE, mTravelTitle);
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_REF, mTravelId);
                startActivity(remindItemIntent);
                break;
            case 2: // Map Tab
                break;
            default:
        }
    }

}
