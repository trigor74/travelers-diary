package com.travelersdiary.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.fragments.TodoItemViewFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoItemActivity extends BaseActivity {
    @Bind(R.id.todo_item_activity_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_item);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle("Todo item title");
        }

        if (savedInstanceState == null) {
            String key = getIntent().getStringExtra(Constants.KEY_TODO_ITEM_REF);

            TodoItemViewFragment todoItemViewFragment = new TodoItemViewFragment();

            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_TODO_ITEM_REF, key);
            todoItemViewFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, todoItemViewFragment)
                    .commit();
        }
    }
}
