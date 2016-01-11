package com.travelersdiary.activities;

import android.net.Uri;
import android.os.Bundle;

import com.firebase.client.AuthData;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.travelersdiary.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BaseActivity extends GoogleOAuthActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.profile_cover_image)
    ImageView mProfileCoverImage;

    @Bind(R.id.profile_image)
    ImageView mProfileImage;

    @Bind(R.id.account_name_text_view)
    TextView mAccountName;

    @Bind(R.id.account_email_text_view)
    TextView mAccountEmail;

    protected NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
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
    protected void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            String name = (String) authData.getProviderData().get("displayName");
            String email = (String) authData.getProviderData().get("email");
            Uri profileImageURL = Uri.parse((String) authData.getProviderData().get("profileImageURL"));
            String UUID = authData.getUid();

            mAccountName.setText(name);
            mAccountEmail.setText(email);
            Picasso.with(this).load(profileImageURL).into(mProfileImage);
        }
    }
}
