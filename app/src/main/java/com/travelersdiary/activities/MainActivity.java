package com.travelersdiary.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.R;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.dialogs.EditTravelDialog;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.fragments.TravelsListFragment;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.services.SyncService;

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
    public void onFabClick() {
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
                Intent remindItemIntent = new Intent(this, ReminderItemActivity.class);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncServiceEnabled = sharedPreferences.getBoolean("sync_service_check_box", true);

        if (savedInstanceState == null) {
            if (!isServiceRunning(SyncService.class) && syncServiceEnabled) {
                Intent intent = new Intent(this, SyncService.class);
                startService(intent);
            }
        }

        setupViewPager();

        openSelectedTab(getIntent());
    }

    public void setupViewPager() {
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.travels_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.diary_text));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.reminder_text));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (getSupportFragmentManager(), MainActivity.this, mTabLayout.getTabCount());

        adapter.addFragment(new TravelsListFragment());
        adapter.addFragment(new DiaryListFragment());
        adapter.addFragment(new ReminderListFragment());

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
                if (tab.getPosition() > 0) {
                    Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getItem(tab.getPosition());
                    try {
                        IActionModeFinishCallback actionModeFinishCallback = (IActionModeFinishCallback) fragment;
                        actionModeFinishCallback.finishActionMode();
                    } catch (ClassCastException e) {
                        throw new ClassCastException(fragment.toString()
                                + " must implement IActionModeFinishCallback");
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openSelectedTab(intent);
    }

    @Override
    protected boolean useDrawerToggle() {
        return true;
    }

    private void openSelectedTab(Intent intent) {
        int tabToOpen = intent.getIntExtra(KEY_TAB_POSITION, 0);

        mTabLayout.getTabAt(tabToOpen).select();
        mNavigationView.getMenu().getItem(tabToOpen).setChecked(true);
    }

    private boolean isTabletLandMode() {
        return getResources().getBoolean(R.bool.isTabletLand);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
