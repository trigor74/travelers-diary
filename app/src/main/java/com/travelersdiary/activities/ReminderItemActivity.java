package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.fragments.ReminderItemFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderItemActivity extends BaseActivity {
    private static final String REMIND_ITEM_FRAGMENT_TAG = "REMIND_ITEM_FRAGMENT_TAG";

    @Bind(R.id.remind_item_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_item);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Remind item title");
        }

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
        if (reminderItemFragment != null) {
            reminderItemFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
