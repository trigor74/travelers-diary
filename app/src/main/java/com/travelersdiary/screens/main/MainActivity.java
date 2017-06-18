package com.travelersdiary.screens.main;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;
import com.travelersdiary.R;
import com.travelersdiary.base.BaseActivity;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.screens.diary.DiaryListFragment;
import com.travelersdiary.screens.reminder.ReminderListFragment;
import com.travelersdiary.screens.travel.TravelsListFragment;
import com.travelersdiary.screens.diary.DiaryActivity;
import com.travelersdiary.screens.reminder.ReminderItemActivity;
import com.travelersdiary.screens.travel.EditTravelActivity;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.services.SyncService;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    public static final String TRAVELS_LIST_FRAGMENT_TAG = "TRAVELS_LIST_FRAGMENT_TAG";
    public static final String DIARY_LIST_FRAGMENT_TAG = "DIARY_LIST_FRAGMENT_TAG";
    public static final String REMINDER_LIST_FRAGMENT_TAG = "REMINDER_LIST_FRAGMENT_TAG";

    public static final String KEY_FRAGMENT = "KEY_FRAGMENT";
    private String mFragmentKey = TRAVELS_LIST_FRAGMENT_TAG;

    private FragmentManager mFragmentManager;
    private DiaryListFragment mDiaryListFragment;
    private ReminderListFragment mReminderListFragment;
    private TravelsListFragment mTravelsListFragment;

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @OnClick(R.id.main_activity_fab)
    public void onFabClick() {
        Intent intent;
        switch (mFragmentKey) {
            case DIARY_LIST_FRAGMENT_TAG:
                intent = new Intent(this, DiaryActivity.class);
                intent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
                startActivity(intent);
                return;
            case REMINDER_LIST_FRAGMENT_TAG:
                intent = new Intent(this, ReminderItemActivity.class);
                startActivity(intent);
                return;
            case TRAVELS_LIST_FRAGMENT_TAG:
            default:
                intent = new Intent(this, EditTravelActivity.class);
                startActivity(intent);
                return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BusProvider.bus().register(this);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean syncServiceEnabled = sharedPreferences.getBoolean("sync_service_check_box", false);

        mDiaryListFragment = new DiaryListFragment();
        mReminderListFragment = new ReminderListFragment();
        mTravelsListFragment = new TravelsListFragment();

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getExtras() != null) {
                mFragmentKey = getIntent().getExtras().getString(KEY_FRAGMENT, TRAVELS_LIST_FRAGMENT_TAG);
            } else {
                mFragmentKey = TRAVELS_LIST_FRAGMENT_TAG;
            }
            mFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, getFragmentByKey(mFragmentKey), mFragmentKey)
                    .commit();

            if (!isServiceRunning(SyncService.class) && syncServiceEnabled) {
                Intent intent = new Intent(this, SyncService.class);
                startService(intent);
            }
        } else {
            mFragmentKey = savedInstanceState.getString(KEY_FRAGMENT, TRAVELS_LIST_FRAGMENT_TAG);
            replaceFragment(mFragmentKey);
        }
    }

    private Fragment getFragmentByKey(String key) {
        switch (key) {
            case DIARY_LIST_FRAGMENT_TAG:
                return mDiaryListFragment;
            case REMINDER_LIST_FRAGMENT_TAG:
                return mReminderListFragment;
            case TRAVELS_LIST_FRAGMENT_TAG:
            default:
                return mTravelsListFragment;
        }
    }

    private void replaceFragment(String fragmentKey) {
        Fragment fragment = mFragmentManager.findFragmentByTag(fragmentKey);
        if (fragment != null) {
            return;
        }
        mFragmentKey = fragmentKey;
        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, getFragmentByKey(mFragmentKey), mFragmentKey)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (mFragmentKey) {
            case DIARY_LIST_FRAGMENT_TAG:
                super.setCheckedItem(R.id.nav_diary);
                return;
            case REMINDER_LIST_FRAGMENT_TAG:
                super.setCheckedItem(R.id.nav_reminder);
                return;
            case TRAVELS_LIST_FRAGMENT_TAG:
            default:
                super.setCheckedItem(R.id.nav_travels);
        }
    }

    @Override
    protected boolean useDrawerToggle() {
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_travels:
                replaceFragment(TRAVELS_LIST_FRAGMENT_TAG);
                return true;
            case R.id.nav_diary:
                replaceFragment(DIARY_LIST_FRAGMENT_TAG);
                return true;
            case R.id.nav_reminder:
                replaceFragment(REMINDER_LIST_FRAGMENT_TAG);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_FRAGMENT, mFragmentKey);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        BusProvider.bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void checkTracking(LocationTrackingService.CheckTrackingEvent event) {
        switchStartStop(event.isTrackingEnabled);
    }

}
