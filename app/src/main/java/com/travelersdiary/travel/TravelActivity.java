package com.travelersdiary.travel;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.BaseActivity;
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.activities.EditTravelActivity;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.interfaces.IFABCallback;
import com.travelersdiary.models.Travel;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.ui.FABScrollBehavior;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

public class TravelActivity extends BaseActivity implements IFABCallback {

    @Bind(R.id.travel_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.travel_tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.travel_view_pager)
    ViewPager mViewPager;

    @Bind(R.id.travel_activity_fab)
    FloatingActionButton mTravelActivityFab;

    private Travel travel;
    private String travelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        BusProvider.bus().register(this);

        travel = (Travel) getIntent().getSerializableExtra("Travel");
        travelId = getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(travel.getTitle());
        }

        setupViewPager();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        if (travelId != null) {
            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID)).child(travelId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Travel newTravel = dataSnapshot.getValue(Travel.class);

                    if (newTravel != null) {
                        travel = newTravel;

                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(travel.getTitle());
                        }
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
                    hideFloatingActionButton(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() < 2) {
                    finishActionMode(tab.getPosition());
                }

                if (tab.getPosition() == 2) {
                    hideFloatingActionButton(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void finishActionMode(int tabPosition) {
        Fragment fragment = ((ViewPagerAdapter) mViewPager.getAdapter()).getItem(tabPosition);
        try {
            IActionModeFinishCallback actionModeFinishCallback = (IActionModeFinishCallback) fragment;
            actionModeFinishCallback.finishActionMode();
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString()
                    + " must implement IActionModeFinishCallback");
        }
    }

    @Override
    protected void onDrawerOpened(View drawerView) {
        if (mTabLayout.getSelectedTabPosition() < 2) {
            finishActionMode(mTabLayout.getSelectedTabPosition());
        }
        super.onDrawerOpened(drawerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.travel_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (travel.isActive()) {
            menu.findItem(R.id.action_travel_start).setVisible(false);
            menu.findItem(R.id.action_travel_stop).setVisible(true);
        } else {
            menu.findItem(R.id.action_travel_start).setVisible(true);
            menu.findItem(R.id.action_travel_stop).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_travel_start:
                Utils.startTravel(this, travelId, getString(R.string.default_travel_title));
                return true;
            case R.id.action_travel_stop:
                Utils.stopTravel(this, travelId);
                return true;
            case R.id.action_travel_edit:
                Intent editIntent = new Intent(this, EditTravelActivity.class);
                editIntent.putExtra("Travel", travel);
                editIntent.putExtra(Constants.KEY_TRAVEL_REF, travelId);
                startActivity(editIntent);
                return true;
            case R.id.action_travel_info:
                showInfo();
                return true;
            case R.id.action_travel_delete:
                Utils.deleteTravel(this, travelId);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfo() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_info);
        dialog.setTitle("Travel info");

        TextView title = (TextView) dialog.findViewById(R.id.travel_title_info);
        title.setText(travel.getTitle());
        TextView description = (TextView) dialog.findViewById(R.id.travel_description_info);
        description.setText(travel.getDescription());

        LinearLayout startLayout = (LinearLayout) dialog.findViewById(R.id.start_time_layout);
        TextView start = (TextView) dialog.findViewById(R.id.travel_start_time);
        if (travel.getStart() != -1) {
            startLayout.setVisibility(View.VISIBLE);
            start.setText(String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getStart()),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getStart())));
        } else {
            startLayout.setVisibility(View.GONE);
        }

        LinearLayout stopLayout = (LinearLayout) dialog.findViewById(R.id.end_time_layout);
        TextView stop = (TextView) dialog.findViewById(R.id.travel_stop_time);
        if (travel.getStop() != -1) {
            stopLayout.setVisibility(View.VISIBLE);
            stop.setText(String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getStop()),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getStop())));
        } else {
            stopLayout.setVisibility(View.GONE);
        }

        LinearLayout creationLayout = (LinearLayout) dialog.findViewById(R.id.creation_time_layout);
        TextView creation = (TextView) dialog.findViewById(R.id.travel_creation_time);
        if (travel.getCreationTime() != -1) {
            creationLayout.setVisibility(View.VISIBLE);
            creation.setText(String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getCreationTime()),
                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getCreationTime())));
        } else {
            creationLayout.setVisibility(View.GONE);
        }

        Button closeDialog = (Button) dialog.findViewById(R.id.btn_close_info);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @OnClick(R.id.travel_activity_fab)
    public void onFabClick() {
        switch (mViewPager.getCurrentItem()) {
            case 0: // Diary Tab
                Intent diaryIntent = new Intent(this, DiaryActivity.class);
                diaryIntent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
                diaryIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                diaryIntent.putExtra(Constants.KEY_TRAVEL_REF, travelId);
                startActivity(diaryIntent);
                break;
            case 1: // Reminder Tab
                Intent remindItemIntent = new Intent(this, ReminderItemActivity.class);
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_REF, travelId);
                startActivity(remindItemIntent);
                break;
            case 2: // Map Tab
                break;
            default:
        }
    }

    @Override
    public void hideFloatingActionButton(boolean hide) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mTravelActivityFab.getLayoutParams();
        if (hide) {
            params.setBehavior(null);
            mTravelActivityFab.setLayoutParams(params);
            mTravelActivityFab.hide();
        } else {
            params.setBehavior(new FABScrollBehavior());
            mTravelActivityFab.setLayoutParams(params);
            mTravelActivityFab.show();
        }
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
