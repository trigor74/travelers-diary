package com.travelersdiary.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Subscribe;
import com.travelersdiary.R;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.fragments.TravelsListFragment;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.services.SyncService;

import butterknife.Bind;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    public static final String TRAVELS_LIST_FRAGMENT_TAG = "TRAVELS_LIST_FRAGMENT_TAG";

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @OnClick(R.id.main_activity_fab)
    public void onFabClick() {
        Intent travelIntent = new Intent(this, EditTravelActivity.class);
        startActivity(travelIntent);
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

        if (savedInstanceState == null) {
            TravelsListFragment fragment = new TravelsListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, TRAVELS_LIST_FRAGMENT_TAG)
                    .commit();

            if (!isServiceRunning(SyncService.class) && syncServiceEnabled) {
                Intent intent = new Intent(this, SyncService.class);
                startService(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.setCheckedItem(R.id.nav_travels);
        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
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
    protected void onDestroy() {
        BusProvider.bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void checkTracking(LocationTrackingService.CheckTrackingEvent event) {
        switchStartStop(event.isTrackingEnabled);
    }

}
