package com.travelersdiary.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.models.Travel;

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

    private String mTravelTitle;
    private String mTravelId;
    private String mTravelDescription;
    private String mTravelDefaultCover;
    private String mTravelUserCover;

    private Menu mMenu;
    private boolean isTravelActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);

        mTravelTitle = getIntent().getStringExtra(Constants.KEY_TRAVEL_TITLE);
        mTravelId = getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);
        mTravelDescription = getIntent().getStringExtra(Constants.KEY_TRAVEL_DESCRIPTION);
        mTravelDefaultCover = getIntent().getStringExtra(Constants.KEY_TRAVEL_DEFAULT_COVER);
        mTravelUserCover = getIntent().getStringExtra(Constants.KEY_TRAVEL_USER_COVER);
        isTravelActive = getIntent().getBooleanExtra(Constants.KEY_TRAVEL_IS_ACTIVE, false);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(mTravelTitle);
        }

        setupViewPager();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        if (mTravelId != null) {
            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID)).child(mTravelId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Travel travel = dataSnapshot.getValue(Travel.class);

                    if (travel != null) {
                        getSupportActionBar().setTitle(travel.getTitle());
                        isTravelActive = travel.isActive();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
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
                    Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getItem(tab.getPosition());
                    try {
                        IActionModeFinishCallback actionModeFinishCallback = (IActionModeFinishCallback) fragment;
                        actionModeFinishCallback.finishActionMode();
                    } catch (ClassCastException e) {
                        throw new ClassCastException(fragment.toString()
                                + " must implement IActionModeFinishCallback");
                    }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.travel_activity_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isTravelActive) {
            menu.findItem(R.id.action_travel_start).setVisible(false);
            menu.findItem(R.id.action_travel_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_travel_start).setVisible(true);
            menu.findItem(R.id.action_travel_stop).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_travel_start:
                Utils.startTravel(this, mTravelId, getString(R.string.default_travel_title));
                return true;
            case R.id.action_travel_stop:
                Utils.stopTravel(this, mTravelId);
                return true;
            case R.id.action_travel_edit:
                Intent editIntent = new Intent(this, EditTravelActivity.class);
                editIntent.putExtra(Constants.KEY_TRAVEL_REF, mTravelId);
                editIntent.putExtra(Constants.KEY_TRAVEL_TITLE, mTravelTitle);
                editIntent.putExtra(Constants.KEY_TRAVEL_DESCRIPTION, mTravelDescription);
                editIntent.putExtra(Constants.KEY_TRAVEL_DEFAULT_COVER, mTravelDefaultCover);
                editIntent.putExtra(Constants.KEY_TRAVEL_USER_COVER, mTravelUserCover);
                startActivity(editIntent);
                return true;
            case R.id.action_travel_delete:
                Utils.deleteTravel(this, mTravelId);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
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
