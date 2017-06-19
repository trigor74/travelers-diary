package com.travelersdiary.base;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.travelersdiary.App;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.screens.login.LoginActivity;
import com.travelersdiary.screens.main.MainActivity;
import com.travelersdiary.screens.preferences.PreferencesActivity;
import com.travelersdiary.services.LocationTrackingService;

import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    protected NavigationView mNavigationView;
    private Menu mMenu;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Firebase mFirebaseRef;
    private Firebase.AuthStateListener mAuthListener;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        BusProvider.bus().register(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        mAuthListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                /* The user has been logged out */
                if (authData == null) {
                    takeUserToLoginScreenOnUnAuth();
                }
            }
        };

        //App.setListeners();
        ((App) getApplicationContext()).setListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseRef.addAuthStateListener(mAuthListener);

        Intent intentCheckTracking = new Intent(this, LocationTrackingService.class);
        intentCheckTracking.setAction(LocationTrackingService.ACTION_CHECK_TRACKING);
        startService(intentCheckTracking);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);

        FrameLayout activityContainer = (FrameLayout) mDrawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);

        super.setContentView(mDrawerLayout);
        ButterKnife.bind(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mMenu = mNavigationView.getMenu();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
        if (Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(activeTravelKey) || activeTravelKey == null) {
            enableStartTrackingButton(false);
        } else {
            enableStartTrackingButton(true);
        }
    }

    public void setupNavigationView(Toolbar toolbar) {
        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                BaseActivity.this.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                BaseActivity.this.onDrawerClosed(drawerView);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (!useDrawerToggle()) {
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                supportActionBar.setDisplayHomeAsUpEnabled(true);
                supportActionBar.setHomeButtonEnabled(true);
                mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        }
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        View headerView = mNavigationView.getHeaderView(0);
        ImageView mCoverImage = (ImageView) headerView.findViewById(R.id.profile_cover_image);
        ImageView mProfileImage = (ImageView) headerView.findViewById(R.id.profile_image);
        TextView mAccountName = (TextView) headerView.findViewById(R.id.account_name_text_view);
        TextView mAccountEmail = (TextView) headerView.findViewById(R.id.account_email_text_view);

        String coverImageUrl = mSharedPreferences.getString(Constants.KEY_COVER_IMAGE, null);
        if (coverImageUrl != null) {
            Glide.with(this).load(coverImageUrl).into(mCoverImage);
        }

        String profileImageUrl = mSharedPreferences.getString(Constants.KEY_PROFILE_IMAGE, null);
        if (profileImageUrl != null) {
            Glide.with(this).load(profileImageUrl).into(mProfileImage);
        }

        mAccountName.setText(mSharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null));
        mAccountEmail.setText(mSharedPreferences.getString(Constants.KEY_EMAIL, null));
    }

    protected boolean useDrawerToggle() {
        return false;
    }

    protected void onDrawerOpened(View drawerView) {
    }

    protected void onDrawerClosed(View drawerView) {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_travels:
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.TRAVELS_LIST_FRAGMENT_TAG);
                startActivity(intent);
                return true;
            case R.id.nav_diary:
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.DIARY_LIST_FRAGMENT_TAG);
                startActivity(intent);
                return true;
            case R.id.nav_reminder:
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(MainActivity.KEY_FRAGMENT, MainActivity.REMINDER_LIST_FRAGMENT_TAG);
                startActivity(intent);
                return true;
            case R.id.nav_start_tracking:
                startTracking();
                return true;
            case R.id.nav_stop_tracking:
                stopTracking();
                return true;
            case R.id.nav_settings:
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void startTracking() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
        if (Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(activeTravelKey)) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            }
            new AlertDialog.Builder(this)
                    .setInverseBackgroundForced(true)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.dialog_cannot_tracking_title_text))
                    .setMessage(getString(R.string.dialog_cannot_tracking_message_text))
                    .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // nothing
                        }
                    })
                    .show();

            switchStartStop(false);
            return;
        }
        Intent intentStartTracking = new Intent(this, LocationTrackingService.class);
        intentStartTracking.setAction(LocationTrackingService.ACTION_START_TRACK);
        startService(intentStartTracking);

        switchStartStop(true);
    }

    public void stopTracking() {
        Intent intentStopTracking = new Intent(this, LocationTrackingService.class);
        intentStopTracking.setAction(LocationTrackingService.ACTION_STOP_TRACK);
        startService(intentStopTracking);

        switchStartStop(false);
    }

    public void enableStartTrackingButton(boolean enable) {
        mMenu.findItem(R.id.nav_start_tracking).setEnabled(enable);
    }

    public void switchStartStop(boolean isStarted) {
        mMenu.findItem(R.id.nav_start_tracking).setVisible(!isStarted);
        mMenu.findItem(R.id.nav_stop_tracking).setVisible(isStarted);
    }

    public void setCheckedItem(int id) {
        mNavigationView.setCheckedItem(id);
    }

}
