package com.travelersdiary.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.services.LocationTrackingService;

import java.util.Map;

import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    protected NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private Firebase mFirebaseRef;
    private Firebase.AuthStateListener mAuthListener;
    private Query mActiveTravelQuery = null;
    private ValueEventListener mActiveTravelListener;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mActiveTravelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    String activeTravelKey = (String) map.get(Constants.FIREBASE_ACTIVE_TRAVEL_KEY);
                    String activeTravelTitle = (String) map.get(Constants.FIREBASE_ACTIVE_TRAVEL_TITLE);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String currentTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);

                    if (currentTravelKey == null || !currentTravelKey.equals(activeTravelKey)) {
                        // CHANGE ACTIVE TRAVEL HERE!!!
                        // switch active travel logic:
                        // 1. disable notifications for active travel 's reminder items
                        // 2. set "active" to false for active travel 's reminder items
                        // 3. set "active" to false for active travel
                        // 4. set "active" to true for new travel
                        // 5. set "active" to true for new travel 's reminder items
                        // 6. enable notifications for new travel 's reminder items
                        // 7. put new active travel's key and title to SharedPreferences

                        // TODO: 05.04.16 ADD CHANGE ACTIVE TRAVEL LOGIC

                        // 7.
                        sharedPreferences.edit()
                                .putString(Constants.KEY_ACTIVE_TRAVEL_KEY, activeTravelKey)
                                .apply();
                        sharedPreferences.edit()
                                .putString(Constants.KEY_ACTIVE_TRAVEL_TITLE, activeTravelTitle)
                                .apply();
                    } else {
                        // is changed title?
                        // TODO: 05.04.16 add a check
                        sharedPreferences.edit()
                                .putString(Constants.KEY_ACTIVE_TRAVEL_TITLE, activeTravelTitle)
                                .apply();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseRef.addAuthStateListener(mAuthListener);
        String userUID = mSharedPreferences.getString(Constants.KEY_USER_UID, null);
        if (userUID != null) {
            mActiveTravelQuery = new Firebase(Utils.getFirebaseUserActiveTravelUrl(userUID));
            mActiveTravelQuery.addValueEventListener(mActiveTravelListener);
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);

        FrameLayout activityContainer = (FrameLayout) mDrawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);

        super.setContentView(mDrawerLayout);
        ButterKnife.bind(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
    }

    protected void setupNavigationView(Toolbar toolbar) {
        mNavigationView.setNavigationItemSelectedListener(this);

        if (useDrawerToggle()) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }

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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);

        switch (item.getItemId()) {
            case R.id.nav_travels:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 0);
                startActivity(intent);
                return true;
            case R.id.nav_diary:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 1);
                startActivity(intent);
                return true;
            case R.id.nav_reminder:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 2);
                startActivity(intent);
                return true;
            case R.id.nav_start_tracking:
                Intent intentStartTracking = new Intent(this, LocationTrackingService.class);
                intentStartTracking.setAction(LocationTrackingService.ACTION_START_TRACK);
                startService(intentStartTracking);
                return true;
            case R.id.nav_stop_tracking:
                Intent intentStopTracking = new Intent(this, LocationTrackingService.class);
                intentStopTracking.setAction(LocationTrackingService.ACTION_STOP_TRACK);
                startService(intentStopTracking);
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
        mFirebaseRef.removeAuthStateListener(mAuthListener);
        mActiveTravelQuery.removeEventListener(mActiveTravelListener);
        super.onPause();
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
