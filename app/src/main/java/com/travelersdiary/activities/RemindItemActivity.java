package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.fragments.RemindItemFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RemindItemActivity extends BaseActivity {
    @Bind(R.id.remind_item_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind_item);

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

            RemindItemFragment todoItemFragment = new RemindItemFragment();

            if (key != null && !key.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_REMINDER_ITEM_REF, key);
                todoItemFragment.setArguments(bundle);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, todoItemFragment)
                    .commit();
        }
    }
}
