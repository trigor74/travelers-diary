package com.travelersdiary.activities;

import android.os.Bundle;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryFragment;

public class DiaryActivity extends BaseActivity {

    public static final String NEW_DIARY_NOTE = "new diary note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        boolean isNewNote = getIntent().getBooleanExtra(DiaryActivity.NEW_DIARY_NOTE, false);

        String travelTitle = getIntent().getStringExtra(Constants.KEY_TRAVEL_TITLE);
        String travelId = getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);

        if (savedInstanceState == null) {
            DiaryFragment diaryFragment = new DiaryFragment();
            Bundle bundle = new Bundle();

            if (isNewNote) {
                bundle.putBoolean(DiaryActivity.NEW_DIARY_NOTE, true);
                bundle.putString(Constants.KEY_TRAVEL_TITLE, travelTitle);
                bundle.putString(Constants.KEY_TRAVEL_REF, travelId);
            } else {
                String key = getIntent().getStringExtra(Constants.KEY_DAIRY_NOTE_REF);
                bundle.putString(Constants.KEY_DAIRY_NOTE_REF, key);
            }

            diaryFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, diaryFragment, DiaryFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DiaryFragment diaryFragment = (DiaryFragment) getSupportFragmentManager()
                .findFragmentByTag(DiaryFragment.class.getSimpleName());
        if (isDrawerOpen()) {
            super.onBackPressed();
        } else if (diaryFragment != null) {
            diaryFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

}
