package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.travelersdiary.R;
import com.travelersdiary.adapters.ViewPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Bind(R.id.main_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    @Bind(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.view_pager)
    ViewPager mViewPager;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setupDrawerContent(mNavigationView);
        mDrawerToggle = setupDrawerToggle();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupViewPager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        onDrawerItemSelected(menuItem);
                        menuItem.setChecked(false);
                        return true;
                    }
                });

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
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

    public void onDrawerItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_travels:
                mDrawerLayout.closeDrawers();
                mViewPager.setCurrentItem(0);
                break;
            case R.id.nav_diary:
                mDrawerLayout.closeDrawers();
                mViewPager.setCurrentItem(1);
                break;
            case R.id.nav_reminder:
                mDrawerLayout.closeDrawers();
                mViewPager.setCurrentItem(2);
                break;
            default:
        }
    }

}
