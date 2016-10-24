package com.travelersdiary.travel;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.BaseActivity;
import com.travelersdiary.activities.EditTravelActivity;
import com.travelersdiary.adapters.ViewPagerAdapter;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.databinding.ActivityTravelBinding;
import com.travelersdiary.databinding.DialogInfoBinding;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.interfaces.IFABCallback;
import com.travelersdiary.models.Travel;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.ui.FABScrollBehavior;

import timber.log.Timber;

public class TravelActivity extends BaseActivity implements IFABCallback, TravelView {

    private Travel travel;
    private String travelId;

    private ActivityTravelBinding binding;
    private TravelViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_travel);

        travel = (Travel) getIntent().getSerializableExtra("Travel");
        travelId = getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);

        viewModel = new TravelViewModel(this, travel);
        binding.setViewModel(viewModel);

        BusProvider.bus().register(this);

        setSupportActionBar((Toolbar) binding.travelActivityToolbar);
        setupNavigationView((Toolbar) binding.travelActivityToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(travel.getTitle());
        }

        setupViewPager(binding.travelTabLayout, binding.travelViewPager);

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
                    Timber.e("Firebase error: %s", firebaseError.getMessage());
                }
            });
        }
    }

    public void setupViewPager(TabLayout tabLayout, final ViewPager viewPager) {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.diary_text));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.reminder_text));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.map_text));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (getSupportFragmentManager(), TravelActivity.this, tabLayout.getTabCount());

        adapter.addFragment(new DiaryListFragment());
        adapter.addFragment(new ReminderListFragment());
        adapter.addFragment(new MapFragment());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

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
        Fragment fragment = ((ViewPagerAdapter) binding.travelViewPager.getAdapter()).getItem(tabPosition);
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
        if (binding.travelTabLayout.getSelectedTabPosition() < 2) {
            finishActionMode(binding.travelTabLayout.getSelectedTabPosition());
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

    @Override
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
                showInfoDialog();
                return true;
            case R.id.action_travel_delete:
                Utils.deleteTravel(this, travelId);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showInfoDialog() {
        final Dialog dialog = new Dialog(this);

        DialogInfoBinding binding = DataBindingUtil.inflate(LayoutInflater.from(dialog.getContext()), R.layout.dialog_info, null, false);
        setContentView(binding.getRoot());

        dialog.setTitle("Travel info");

        binding.btnCloseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public int getPagerCurrentItem() {
        return binding.travelViewPager.getCurrentItem();
    }

    @Override
    public void hideFloatingActionButton(boolean hide) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.travelActivityFab.getLayoutParams();
        if (hide) {
            params.setBehavior(null);
            binding.travelActivityFab.setLayoutParams(params);
            binding.travelActivityFab.hide();
        } else {
            params.setBehavior(new FABScrollBehavior());
            binding.travelActivityFab.setLayoutParams(params);
            binding.travelActivityFab.show();
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
