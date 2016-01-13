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
import android.widget.FrameLayout;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.travelersdiary.Constants;
import com.travelersdiary.R;

public class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    protected NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Firebase.AuthStateListener mAuthListener;
    private Firebase mFirebaseRef;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // for example
        String provider = mSharedPreferences.getString(Constants.KEY_PROVIDER, null);
        String userUID = mSharedPreferences.getString(Constants.KEY_USER_UID, null);
        String userEmail = mSharedPreferences.getString(Constants.KEY_EMAIL, null);
        String userName = mSharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null);
        String userProfileImage = mSharedPreferences.getString(Constants.KEY_PROFILE_IMAGE, null);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseRef.addAuthStateListener(mAuthListener);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mDrawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);

        FrameLayout activityContainer = (FrameLayout) mDrawerLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);

        super.setContentView(mDrawerLayout);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
    }

    protected void setupNavigationView(Toolbar toolbar) {
        mNavigationView.setNavigationItemSelectedListener(this);

        if (useDrawerToggle()) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseRef.removeAuthStateListener(mAuthListener);
    }

    private void takeUserToLoginScreenOnUnAuth() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
