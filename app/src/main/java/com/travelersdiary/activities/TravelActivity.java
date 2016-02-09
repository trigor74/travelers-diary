package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.adapters.ViewPagerAdapter;

import butterknife.Bind;

public class TravelActivity extends BaseActivity {

    @Bind(R.id.travel_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.travel_tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.travel_view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
//            supportActionBar.setTitle(R.string.travel_activity_title);
            String travelTitle = getIntent().getStringExtra(Constants.KEY_TRAVEL_TITLE);
            if (travelTitle == null || travelTitle.isEmpty()) {
                travelTitle = getString(R.string.travel_activity_title);
            }
            supportActionBar.setTitle(travelTitle);
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
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.nav_travels:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 0);
                startActivity(intent);
                break;
            case R.id.nav_diary:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 1);
                startActivity(intent);
                break;
            case R.id.nav_reminder:
                intent.putExtra(MainActivity.KEY_TAB_POSITION, 2);
                startActivity(intent);
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

}
