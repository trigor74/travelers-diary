package com.travelersdiary.activities;

import android.os.Bundle;

import com.squareup.otto.Subscribe;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.fragments.ReminderItemFragment;
import com.travelersdiary.services.LocationTrackingService;

public class ReminderItemActivity extends BaseActivity {
    private static final String REMIND_ITEM_FRAGMENT_TAG = "REMIND_ITEM_FRAGMENT_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_item);
        BusProvider.bus().register(this);

        if (savedInstanceState == null) {
            String key = getIntent().getStringExtra(Constants.KEY_REMINDER_ITEM_REF);

            ReminderItemFragment reminderItemFragment = new ReminderItemFragment();

            if (key != null && !key.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_REMINDER_ITEM_REF, key);
                reminderItemFragment.setArguments(bundle);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, reminderItemFragment, REMIND_ITEM_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        ReminderItemFragment reminderItemFragment = (ReminderItemFragment) getSupportFragmentManager()
                .findFragmentByTag(REMIND_ITEM_FRAGMENT_TAG);
        if (isDrawerOpen()) {
            super.onBackPressed();
        } else if (reminderItemFragment != null) {
            reminderItemFragment.onBackPressed();
        } else {
            super.onBackPressed();
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
