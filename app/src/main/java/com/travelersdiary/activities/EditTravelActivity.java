package com.travelersdiary.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.Travel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.Bind;
import butterknife.OnClick;

public class EditTravelActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    @Bind(R.id.edit_travel_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.edit_travel_app_bar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.edit_travel_background)
    ImageView mTravelBackground;

    @Bind(R.id.edit_travel_cover_fab)
    FloatingActionButton mEditTravelCoverFab;

    @Bind(R.id.input_travel_title)
    EditText mTravelTitle;

    @Bind(R.id.input_travel_description)
    EditText mTravelDescription;

    private String mTravelKey;
    private String mTravelDefaultCover;
    private String mTravelUserCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_travel);

        setSupportActionBar(mToolbar);
        setupNavigationView(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            supportActionBar.setTitle(R.string.new_travel);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTravelTitle.setText(extras.getString(Constants.KEY_TRAVEL_TITLE));
            mTravelDescription.setText(extras.getString(Constants.KEY_TRAVEL_DESCRIPTION));
            mTravelKey = extras.getString(Constants.KEY_TRAVEL_REF);
            mTravelDefaultCover = extras.getString(Constants.FIREBASE_TRAVEL_DEFAULT_COVER);
            mTravelUserCover = extras.getString(Constants.FIREBASE_TRAVEL_USER_COVER);
            if (mTravelKey != null && !mTravelKey.isEmpty()) {
                if (supportActionBar != null) supportActionBar.setTitle(R.string.edit_travel);
            }
        } else {
            mTravelDefaultCover = randomTravelCover();
        }

        Glide.with(EditTravelActivity.this)
                .load(mTravelUserCover == null ? mTravelDefaultCover : mTravelUserCover)
                .centerCrop()
                .crossFade()
                .into(mTravelBackground);

        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        if (percentage >= 0.6f) {
            mEditTravelCoverFab.hide();
        } else {
            mEditTravelCoverFab.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {

            mTravelUserCover = data.getDataString();

            Glide.with(EditTravelActivity.this)
                    .load(mTravelUserCover)
                    .centerCrop()
                    .crossFade()
                    .into(mTravelBackground);
        }
    }

    @OnClick(R.id.save_travel_button)
    public void onSaveTravelButtonClick() {
        if (save()) {
            finish();
        }
    }

    @OnClick(R.id.edit_travel_cover_fab)
    public void onEditTravelCoverFabClick() {
        pickImage();
    }

    private boolean save() {
        long currentTime = System.currentTimeMillis();

        String title;

        if (Utils.isEmpty(mTravelTitle)) {
            mTravelTitle.requestFocus();
            Toast.makeText(EditTravelActivity.this, R.string.title_field_is_empty, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            title = mTravelTitle.getText().toString();
        }

        String description = mTravelDescription.getText().toString();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EditTravelActivity.this);
        final String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));

        if (mTravelKey == null || mTravelKey.isEmpty()) {
            // create
            Travel travel = new Travel();
            travel.setTitle(title);
            travel.setDescription(description);
            travel.setStart(currentTime);
            travel.setStop(-1);
            travel.setActive(false); // TODO: why not active if start time == creation time?
            travel.setDefaultCover(mTravelDefaultCover);
            travel.setUserCover(mTravelUserCover);

            Firebase newTravelRef = firebaseRef.push();
            newTravelRef.setValue(travel);
        } else {
            // edit
            Map<String, Object> map = new HashMap<>();
            map.put(Constants.FIREBASE_TRAVEL_TITLE, title);
            map.put(Constants.FIREBASE_TRAVEL_DESCRIPTION, description);
            map.put(Constants.FIREBASE_TRAVEL_USER_COVER, mTravelUserCover);
            Firebase editTravelRef = firebaseRef.child(mTravelKey);
            editTravelRef.updateChildren(map);

            // update all notes with new travel title
            final String newTravelTitle = title;

            Firebase diaryRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
            Query queryRef = diaryRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(mTravelKey);

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.FIREBASE_DIARY_TRAVEL_TITLE, newTravelTitle);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        child.getRef().updateChildren(map);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

            // update all reminder items with new travel title
            Firebase reminderRef = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));
            Query reminderQueryRef = reminderRef.orderByChild(Constants.FIREBASE_REMINDER_TRAVELID).equalTo(mTravelKey);

            reminderQueryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.FIREBASE_REMINDER_TRAVEL_TITLE, newTravelTitle);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        child.getRef().updateChildren(map);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });

            // update active travel title if need
            String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            if (mTravelKey.equals(activeTravelKey)) {
                Map<String, Object> activeTravelMap = new HashMap<>();
                activeTravelMap.put(Constants.FIREBASE_ACTIVE_TRAVEL_TITLE, title);
                activeTravelMap.put(Constants.FIREBASE_ACTIVE_TRAVEL_KEY, mTravelKey);
                Firebase activeTravelRef = new Firebase(Utils.getFirebaseUserActiveTravelUrl(userUID));
                activeTravelRef.setValue(activeTravelMap);
            }
        }

        Toast.makeText(EditTravelActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void pickImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, Constants.PICK_IMAGE_REQUEST_CODE);
    }

    private String randomTravelCover() {
        Random random = new Random();
        int imageNumber = random.nextInt(Constants.TRAVEL_DEFAULT_COVER_COUNT - 1) + 1;

        return ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.travel_cover_1)
                + '/' + getResources().getResourceTypeName(R.drawable.travel_cover_1)
                + '/' + "travel_cover_" + imageNumber;
    }

}
