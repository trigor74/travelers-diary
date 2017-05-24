package com.travelersdiary.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;
import com.squareup.otto.Subscribe;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.AlbumImagesActivity;
import com.travelersdiary.activities.BaseActivity;
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.activities.DiaryImagesActivity;
import com.travelersdiary.activities.GalleryAlbumActivity;
import com.travelersdiary.adapters.DiaryImagesListAdapter;
import com.travelersdiary.bus.BusProvider;
import com.travelersdiary.models.AddressDetails;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.Photo;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.WeatherInfo;
import com.travelersdiary.services.GeocoderIntentService;
import com.travelersdiary.services.LocationTrackingService;
import com.travelersdiary.services.WeatherIntentService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener, OnMapReadyCallback {

    @Bind(R.id.fab_edit_diary_note)
    FloatingActionButton mFabEditDiaryNote;

    @Bind(R.id.diary_fragment_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.diary_note_big_title)
    EditText mBigTitle;

    @Bind(R.id.diary_note_date)
    TextView mDateTextView;

    @Bind(R.id.diary_note_time)
    TextView mTimeTextView;

    @Bind(R.id.diary_note_travel)
    TextView mTravelTitle;

    @Bind(R.id.diary_note_app_bar)
    AppBarLayout mAppBarLayout;

    @Bind(R.id.big_title_layout)
    LinearLayout mBigTitleLayout;

    @Bind(R.id.diary_note_content)
    NestedScrollView mContentScrollView;

    @Bind(R.id.rte_toolbar_container)
    ViewGroup mToolbarContainer;

    @Bind(R.id.rte_toolbar)
    RTToolbar mRtToolbar;

    @Bind(R.id.rt_editor)
    RTEditText mRtEditText;

    @Bind(R.id.images_list)
    RecyclerView mImagesRecyclerView;

    @Bind(R.id.warning)
    LinearLayout mWarning;

    @Bind(R.id.warning_checkbox)
    AppCompatCheckBox mWarningCheckbox;

    @Bind(R.id.diary_footer_layout)
    LinearLayout mDiaryFooterLayout;

    @Bind(R.id.diary_weather_layout)
    LinearLayout mWeatherLayout;

    @Bind(R.id.img_weather_icon)
    ImageView mImgWeatherIcon;

    @Bind(R.id.txt_weather_info)
    TextView mTxtWeatherInfo;

    @Bind(R.id.diary_location_layout)
    LinearLayout mLocationLayout;

    @Bind(R.id.txt_location_1)
    TextView mTxtLocation;

    @Bind(R.id.txt_location_2)
    TextView mTxtSubLocation;

    @Bind(R.id.location_drop_down)
    ImageView mLocationDropDown;

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean isTitleVisible = false;
    private boolean isEditingMode;
    private boolean isNewDiaryNote;
    private boolean showSavedToast;

    private SharedPreferences mSharedPreferences;
    private ActionBar mSupportActionBar;
    private EditText mEdtDiaryNoteTitle;
    private ArrayList<Photo> mImages = new ArrayList<>();
    private String mImagePath;

    private Firebase mItemRef;
    private ValueEventListener mValueEventListener;
    private FirebaseListAdapter<Travel> mAdapter;
    private String mUserUID;
    private String mKey;
    private DiaryNote mDiaryNote;
    private String mTravelId;

    private RTManager mRtManager;
    private InputMethodManager mInputMethodManager;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // set theme
        getActivity().setTheme(R.style.RteTheme);

        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        ButterKnife.bind(this, view);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = mSharedPreferences.getString(Constants.KEY_USER_UID, null);
        mKey = getArguments().getString(Constants.KEY_DAIRY_NOTE_REF);


        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((BaseActivity) getActivity()).setupNavigationView(mToolbar);

        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            mSupportActionBar.setDisplayShowTitleEnabled(false);
        }

        mImagesRecyclerView.setVisibility(View.GONE);

        mEdtDiaryNoteTitle = (EditText) mToolbar.findViewById(R.id.edt_title);
        mEdtDiaryNoteTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.HIDE);
                    Utils.tintWidget(getContext(), mEdtDiaryNoteTitle, R.color.colorAccent);
                } else {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW);
                    if (mEdtDiaryNoteTitle != null) {
                        Utils.tintWidget(getContext(), mEdtDiaryNoteTitle, R.color.white);
                        if (Utils.isEmpty(mEdtDiaryNoteTitle)) {
                            mEdtDiaryNoteTitle.setText(mDiaryNote.getTitle());
                        }
                    }
                }
            }
        });

        mBigTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.HIDE);
                    Utils.tintWidget(getContext(), mBigTitle, R.color.colorAccent);
                } else {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW);
                    if (mBigTitle != null) {
                        Utils.tintWidget(getContext(), mBigTitle, R.color.white);
                        if (Utils.isEmpty(mBigTitle)) {
                            mBigTitle.setText(mDiaryNote.getTitle());
                        }
                    }
                }
            }
        });

        mRtEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAppBarLayout.setExpanded(false);
                }
            }
        });

        // create RTManager
        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl(getActivity()), new RTMediaFactoryImpl(getContext(), true));
        mRtManager = new RTManager(rtApi, savedInstanceState);

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // register toolbar (if it exists)
        if (mRtToolbar != null) {
            mRtManager.registerToolbar(mToolbarContainer, mRtToolbar);
        }

        // register rich text editor
        mRtManager.registerEditor(mRtEditText, true);

        mAdapter = new FirebaseListAdapter<Travel>(getActivity(), Travel.class,
                android.R.layout.simple_dropdown_item_1line, new Firebase(Utils.getFirebaseUserTravelsUrl(mUserUID))) {
            @Override
            protected void populateView(View view, Travel travel, int position) {
                ((TextView) view.findViewById(android.R.id.text1)).setText(travel.getTitle());
            }
        };

        isNewDiaryNote = getArguments().getBoolean(DiaryActivity.NEW_DIARY_NOTE, false);

        if (isNewDiaryNote) {
            mItemRef = new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID));
            mDiaryNote = new DiaryNote();
            initNewDiaryNote(mDiaryNote);
            enableEditingMode();
            mAppBarLayout.setExpanded(true);
            mBigTitle.requestFocus();
            startLocationRetrieval();
        } else {
            addDataChangeListener();
            enableReviewingMode();
        }

//        setupMap();

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAppBarLayout.addOnOffsetChangedListener(this);
        Utils.startAlphaAnimation(mEdtDiaryNoteTitle, 0, View.INVISIBLE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mImagesRecyclerView.setLayoutManager(layoutManager);

        mImagesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        DiaryImagesListAdapter imagesAdapter = new DiaryImagesListAdapter(this, mImages);
        mImagesRecyclerView.setAdapter(imagesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.bus().register(this);
    }

    @Override
    public void onPause() {
        BusProvider.bus().unregister(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                Photo photo = new Photo(mImagePath);
                mImages.add(0, photo);

                mImagesRecyclerView.setVisibility(View.VISIBLE);

                mImagesRecyclerView.getAdapter().notifyDataSetChanged();
                mImagesRecyclerView.scrollToPosition(0);
            } else {
                new File(mImagePath).delete();
            }
        }

        if (requestCode == Constants.GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> path = data.getStringArrayListExtra(AlbumImagesActivity.SELECTED_IMAGES);

            for (int i = 0; i < path.size(); i++) {
                mImages.add(0, new Photo(path.get(i)));
            }

            if (!mImages.isEmpty()) {
                mImagesRecyclerView.setVisibility(View.VISIBLE);
                mImagesRecyclerView.getAdapter().notifyDataSetChanged();
                mImagesRecyclerView.scrollToPosition(0);
            }
        }

        if (requestCode == Constants.IMAGES_DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            ArrayList<Photo> newImagesList = (ArrayList<Photo>) data.getSerializableExtra(DiaryImagesActivity.IMAGES_AFTER_DELETE);

            mImages.clear();
            mImages.addAll(newImagesList);

            showSavedToast = false;
            saveChanges();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.diary_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isEditingMode) {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            menu.setGroupVisible(R.id.editor_menu, true);
            menu.setGroupVisible(R.id.diary_menu, false);

            PackageManager pm = getActivity().getPackageManager();
            if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                menu.findItem(R.id.action_add_photo).setVisible(false);
            }
        } else {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            menu.setGroupVisible(R.id.editor_menu, false);
            menu.setGroupVisible(R.id.diary_menu, true);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                showSavedToast = true;
                saveChanges();
                return true;
            case R.id.action_diary_share:
                share();
                return true;
            case R.id.action_diary_edit:
                enableEditingMode();
                return true;
            case R.id.action_diary_delete:
                delete();
                return true;
            case R.id.action_add_photo:
                takePhoto();
                return true;
            case R.id.action_add_image:
                pickImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleToolbarTitleVisibility(percentage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRtManager != null) {
            mRtManager.onDestroy(true);
        }

        if (mItemRef != null && mValueEventListener != null) {
            mItemRef.removeEventListener(mValueEventListener);
        }

        mAdapter.cleanup();
    }

    public void onBackPressed() {
        if (isEditingMode && !isNewDiaryNote) {
            if (mRtEditText.hasChanged()) {
                showDiscardDialog();
            } else {
                enableReviewingMode();
            }
        } else {
            getActivity().finish();
        }
    }

    private void enableReviewingMode() {
        isEditingMode = false;

        retrieveData();

        // make edit text field not editable
        makeEditTextEditable(mRtEditText, false);
        mRtEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        //setup title fields
        makeEditTextEditable(mEdtDiaryNoteTitle, false);
        Utils.tintWidget(getContext(), mEdtDiaryNoteTitle, android.R.color.transparent);
        makeEditTextEditable(mBigTitle, false);
        Utils.tintWidget(getContext(), mBigTitle, android.R.color.transparent);

        //setup rte toolbar
        mToolbarContainer.setVisibility(View.GONE);
        mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.HIDE);

        //hide travel title drop down arrow
        mTravelTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        //hide keyboard
        mInputMethodManager.hideSoftInputFromWindow(mRtEditText.getWindowToken(), 0);

        //show fab
        setFabVisibility(mFabEditDiaryNote, View.VISIBLE);

        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    @OnClick(R.id.fab_edit_diary_note)
    public void enableEditingMode() {
        isEditingMode = true;

        //collapse toolbar
        mAppBarLayout.setExpanded(false, true);

        // reset edit text field to editable mode
        mRtEditText.setVisibility(View.VISIBLE);
        makeEditTextEditable(mRtEditText, true);
        mRtEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mRtEditText.requestFocus();
        mRtEditText.setSelection(mRtEditText.getText().length());

        //setup rte toolbar
        mToolbarContainer.setVisibility(View.VISIBLE);
        mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW);

        //setup title fields
        makeEditTextEditable(mEdtDiaryNoteTitle, true);
        Utils.tintWidget(getContext(), mEdtDiaryNoteTitle, R.color.white);
        makeEditTextEditable(mBigTitle, true);
        Utils.tintWidget(getContext(), mBigTitle, R.color.white);

        //show travel title drop down arrow
        mTravelTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.drop_down_arrow, 0);

        //show keyboard
        mInputMethodManager.showSoftInput(mRtEditText, InputMethodManager.SHOW_IMPLICIT);

        //hide fab
        setFabVisibility(mFabEditDiaryNote, View.GONE);

        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void initNewDiaryNote(DiaryNote diaryNote) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String currentTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY);
        String currentTravelTitle = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_TITLE, getString(R.string.default_travel_title));
        String travelTitle = getArguments().getString(Constants.KEY_TRAVEL_TITLE, currentTravelTitle);
        String travelId = getArguments().getString(Constants.KEY_TRAVEL_REF, currentTravelKey);

        diaryNote.setTravelId(travelId);
        diaryNote.setTravelTitle(travelTitle);
        diaryNote.setTime(System.currentTimeMillis());

        Date time = new Date(diaryNote.getTime());
        String date = new SimpleDateFormat("EEEE, MMM dd, yyyy").format(time);
        String timeText = new SimpleDateFormat("HH:mm").format(time);

        mDateTextView.setText(date);
        mTimeTextView.setText(timeText);

        mTravelTitle.setText(diaryNote.getTravelTitle());
    }

    private void addDataChangeListener() {
        mItemRef = new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                .child(mKey);

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDiaryNote = dataSnapshot.getValue(DiaryNote.class);

                if (mDiaryNote == null) {
                    return;
                }

                mEdtDiaryNoteTitle.setText(mDiaryNote.getTitle());
                mBigTitle.setText(mDiaryNote.getTitle());
                mTravelTitle.setText(mDiaryNote.getTravelTitle());

                mRtEditText.setRichTextEditing(true, mDiaryNote.getText());
                if (Utils.isEmpty(mRtEditText)) {
                    mRtEditText.setVisibility(View.GONE);
                } else {
                    mRtEditText.setVisibility(View.VISIBLE);
                }

                if (mDiaryNote.getPhotos() != null && !mDiaryNote.getPhotos().isEmpty()) {
                    mImages = mDiaryNote.getPhotos();

                    boolean showWarning = mSharedPreferences.getBoolean(Constants.KEY_SHOW_WARNING, true);
                    if (showWarning) {
                        for (int i = 0; i < mImages.size(); i++) {
                            if (!Utils.checkFileExists(getContext(), mImages.get(i).getLocalUri()) &&
                                    mImages.get(i).getPicasaUri() == null) {
                                mWarning.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }

                    mImagesRecyclerView.setVisibility(View.VISIBLE);
                    ((DiaryImagesListAdapter) mImagesRecyclerView.getAdapter()).changeList(mImages);
                    mImagesRecyclerView.scrollToPosition(0);
                } else {
                    mImagesRecyclerView.setVisibility(View.GONE);
                }

                mDiaryFooterLayout.setVisibility(View.VISIBLE);
                setWeatherViews();

                if (mDiaryNote.getLocation() != null) {
                    mLocationLayout.setVisibility(View.VISIBLE);
                    setupMap();

                    if (mDiaryNote.getAddressDetails() != null) {
                        setLocationText(mDiaryNote.getAddressDetails());
                    } else {
                        mTxtLocation.setText(getResources()
                                .getString(R.string.location_format_address_line_with_gps,
                                        mDiaryNote.getLocation().getLatitude(),
                                        mDiaryNote.getLocation().getLongitude()));
                        mTxtSubLocation.setVisibility(View.GONE);
                    }

                    putMarker(new LatLng(mDiaryNote.getLocation().getLatitude(),
                            mDiaryNote.getLocation().getLongitude()));
                } else {
                    mLocationLayout.setVisibility(View.GONE);

                    if (mDiaryNote.getWeather() == null) {
                        mDiaryFooterLayout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mItemRef.addValueEventListener(mValueEventListener);
    }

    private void retrieveData() {
        new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                .child(mKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDiaryNote = dataSnapshot.getValue(DiaryNote.class);

                        if (mDiaryNote == null) {
                            return;
                        }

                        mEdtDiaryNoteTitle.setText(mDiaryNote.getTitle());
                        mBigTitle.setText(mDiaryNote.getTitle());

                        Date time = new Date(mDiaryNote.getTime());
                        String date = new SimpleDateFormat("EEEE, MMM dd, yyyy").format(time);
                        String timeText = new SimpleDateFormat("HH:mm").format(time);
                        mDateTextView.setText(date);
                        mTimeTextView.setText(timeText);

                        mTravelTitle.setText(mDiaryNote.getTravelTitle());

                        mRtEditText.setRichTextEditing(true, mDiaryNote.getText());
                        if (Utils.isEmpty(mRtEditText)) {
                            mRtEditText.setVisibility(View.GONE);
                        }

                        if (mDiaryNote.getPhotos() != null && !mDiaryNote.getPhotos().isEmpty()) {
                            mImages = mDiaryNote.getPhotos();

                            boolean showWarning = mSharedPreferences.getBoolean(Constants.KEY_SHOW_WARNING, true);
                            if (showWarning) {
                                for (int i = 0; i < mImages.size(); i++) {
                                    if (!Utils.checkFileExists(getContext(), mImages.get(i).getLocalUri()) &&
                                            mImages.get(i).getPicasaUri() == null) {
                                        mWarning.setVisibility(View.VISIBLE);
                                        break;
                                    }
                                }
                            }

                            mImagesRecyclerView.setVisibility(View.VISIBLE);
                            ((DiaryImagesListAdapter) mImagesRecyclerView.getAdapter()).changeList(mImages);
                            mImagesRecyclerView.scrollToPosition(0);
                        } else {
                            mImagesRecyclerView.setVisibility(View.GONE);
                        }

                        mDiaryFooterLayout.setVisibility(View.VISIBLE);
                        setWeatherViews();

                        if (mDiaryNote.getLocation() != null) {
                            mLocationLayout.setVisibility(View.VISIBLE);
                            setupMap();

                            if (mDiaryNote.getAddressDetails() != null) {
                                setLocationText(mDiaryNote.getAddressDetails());
                            } else {
                                mTxtLocation.setText(getResources()
                                        .getString(R.string.location_format_address_line_with_gps,
                                                mDiaryNote.getLocation().getLatitude(),
                                                mDiaryNote.getLocation().getLongitude()));
                                mTxtSubLocation.setVisibility(View.GONE);
                            }

                            putMarker(new LatLng(mDiaryNote.getLocation().getLatitude(),
                                    mDiaryNote.getLocation().getLongitude()));
                        } else {
                            mLocationLayout.setVisibility(View.GONE);

                            if (mDiaryNote.getWeather() == null) {
                                mDiaryFooterLayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLocationText(AddressDetails addressDetails) {
        mTxtLocation.setText(addressDetails.getLocality());
        String subAddress = String.format("%s%s%s%s",
                addressDetails.getFeatureName() == null || addressDetails.getFeatureName().equals("Unnamed Rd") ? "" : addressDetails.getFeatureName() + ", ",
                addressDetails.getThoroughfare() == null || addressDetails.getThoroughfare().equals("Unnamed Rd") ? "" : addressDetails.getThoroughfare() + ", ",
                addressDetails.getAdminArea() == null || addressDetails.getAdminArea().equals("Unnamed Rd") ? "" : addressDetails.getAdminArea() + ", ",
                addressDetails.getCountryName() == null || addressDetails.getCountryName().equals("Unnamed Rd") ? "" : addressDetails.getCountryName());
        mTxtSubLocation.setText(subAddress);
        mTxtSubLocation.setVisibility(View.VISIBLE);
    }

    public void showDiscardDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.discard_changes_text)
                .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // go to review
                        enableReviewingMode();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .show();
    }

    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("MyLog", "Error while creating file. " + ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePhotoIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(getActivity(), GalleryAlbumActivity.class);
        startActivityForResult(intent, Constants.GALLERY_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        String travelTitle = mDiaryNote.getTravelTitle();
        travelTitle = travelTitle.replaceAll(" ", "_");
        travelTitle = travelTitle.toUpperCase();

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = travelTitle + "_" + timeStamp;
        File picturesFolder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File appPicturesFolder = new File(picturesFolder, getString(R.string.app_name));
        if (!appPicturesFolder.exists()) {
            appPicturesFolder.mkdirs();
        }

        File travelPicturesFolder = new File(appPicturesFolder, mDiaryNote.getTravelTitle());
        if (!travelPicturesFolder.exists()) {
            travelPicturesFolder.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,          /* prefix */
                ".jpg",                 /* suffix */
                travelPicturesFolder    /* directory */
        );

        Uri imageUri = Utils.getImageContentUri(getContext(), image);
        if (imageUri != null) {
            mImagePath = imageUri.toString();
        }

        return image;
    }

    private void saveChanges() {
        //save title
        if (isTitleVisible) {
            if (!Utils.isEmpty(mEdtDiaryNoteTitle)) {
                mDiaryNote.setTitle(mEdtDiaryNoteTitle.getText().toString());
            } else {
                Toast.makeText(getContext(), R.string.title_field_is_empty, Toast.LENGTH_SHORT).show();
                mEdtDiaryNoteTitle.requestFocus();
                return;
            }
        } else {
            if (!Utils.isEmpty(mBigTitle)) {
                mDiaryNote.setTitle(mBigTitle.getText().toString());
            } else {
                Toast.makeText(getContext(), R.string.title_field_is_empty, Toast.LENGTH_SHORT).show();
                mBigTitle.requestFocus();
                return;
            }
        }

        //save text
        mDiaryNote.setText(mRtEditText.getText(RTFormat.HTML));

        //save images
        mDiaryNote.setPhotos(mImages);

        if (mKey != null) {
            mItemRef.setValue(mDiaryNote);
        } else {
            Firebase newTravelRef = mItemRef.push();
            newTravelRef.setValue(mDiaryNote);
            mKey = newTravelRef.getKey();
            addDataChangeListener();
        }

        mRtEditText.resetHasChanged();

        enableReviewingMode();

        if (showSavedToast) {
            Toast.makeText(getContext(), R.string.saved, Toast.LENGTH_SHORT).show();
        }
    }

    private void share() {
        String subject = mEdtDiaryNoteTitle.getText().toString();
        String text = mRtEditText.getText().toString();

        ArrayList<Uri> shareImages = new ArrayList<>();
        ArrayList<String> shareLinks = new ArrayList<>();
        String shareText = text + "\n\n";

        for (int i = 0; i < mImages.size(); i++) {
            if (Utils.checkFileExists(getContext(), mImages.get(i).getLocalUri())) {
                shareImages.add(Uri.parse(mImages.get(i).getLocalUri()));
            } else if (mImages.get(i).getPicasaUri() != null) {
                shareLinks.add(mImages.get(i).getPicasaUri());
            }
        }

        for (String link : shareLinks) {
            shareText += link + "\n";
        }

        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_SUBJECT, subject);

        if (!shareImages.isEmpty()) {
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareImages);
        }

        if (shareText.trim().length() != 0) {
            share.putExtra(Intent.EXTRA_TEXT, shareText);
        }

        startActivity(share);
    }

    private void delete() {
        mItemRef.removeEventListener(mValueEventListener);
        mItemRef.removeValue();
        getActivity().finish();
        Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
    }

    private String getStringExtra(Intent intent, String key) {
        String s = intent.getStringExtra(key);
        return s == null ? "" : s;
    }

    private void makeEditTextEditable(EditText editText, boolean editable) {
        editText.setClickable(editable);
        editText.setLongClickable(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
    }

    private void setFabVisibility(FloatingActionButton fab, int visibility) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        if (visibility == View.VISIBLE) {
            // bring things back to normal state
            params.setBehavior(new FloatingActionButton.Behavior());
            params.setAnchorId(R.id.diary_note_app_bar);
            fab.setLayoutParams(params);
        } else {
            // hide fab
            params.setBehavior(null);
            params.setAnchorId(View.NO_ID);
            fab.setLayoutParams(params);
            fab.setVisibility(visibility);
        }
    }

    @OnClick(R.id.diary_note_travel)
    public void onTravelSpinnerClick() {
        if (isEditingMode) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Select travel")
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    })
                    .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Travel travel = mAdapter.getItem(which);
                            mTravelId = mAdapter.getRef(which).getKey();
                            mTravelTitle.setText(travel.getTitle());
                            if (mTravelId != null) {
                                mDiaryNote.setTravelTitle(mTravelTitle.getText().toString());
                                mDiaryNote.setTravelId(mTravelId);
                                mDiaryNote.setPicasaAlbumId(travel.getPicasaAlbumId());
                            }
                        }
                    })
                    .show();
        }
    }

    // location
    private void startLocationRetrieval() {
        Intent intent = new Intent(getContext(), LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_GET_CURRENT_LOCATION);
        getActivity().startService(intent);
    }

    @Subscribe
    public void getLocation(LocationPoint location) {
        if (isNewDiaryNote) {
            mDiaryFooterLayout.setVisibility(View.VISIBLE);
            mLocationLayout.setVisibility(View.VISIBLE);

            mDiaryNote.setLocation(location);
            mTxtLocation.setText(getResources()
                    .getString(R.string.location_format_address_line_with_gps,
                            location.getLatitude(),
                            location.getLongitude()));
            mTxtSubLocation.setVisibility(View.GONE);

            if (mMap == null) {
                setupMap();
            }

            putMarker(location.getLatLng());

            startAddressRetrieval(mDiaryNote.getLocation());
            updateWeather();
        }
    }

    // address
    private boolean isAddressRetrievalInProgress = false;

    private void startAddressRetrieval(LocationPoint location) {
        if (!isAddressRetrievalInProgress && Utils.isInternetAvailable(getContext())) {
            isAddressRetrievalInProgress = true;
            Intent intent = new Intent(getContext(), GeocoderIntentService.class);
            intent.putExtra(GeocoderIntentService.LOCATION_DATA_EXTRA, location);
            getActivity().startService(intent);
        }
    }

    @OnClick(R.id.location_refresh_pin)
    public void updateAddress() {
        if (isNewDiaryNote) {
            startLocationRetrieval();
        } else if (isEditingMode && mDiaryNote != null && mDiaryNote.getLocation() != null) {
            startAddressRetrieval(mDiaryNote.getLocation());
        }
    }

    @Subscribe
    public void getAddress(GeocoderIntentService.GeocoderResult result) {
        isAddressRetrievalInProgress = false;
        if (result != null) {
            if (result.resultCode == GeocoderIntentService.SUCCESS_RESULT && result.addressDetails != null) {
                setLocationText(result.addressDetails);
                mDiaryNote.setAddressDetails(result.addressDetails);
            }
        }
    }

    //weather
    private boolean isWeatherRetrievalInProgress = false;

    private void startWeatherRetrieval(LocationPoint location) {
        if (!isWeatherRetrievalInProgress) {
            isWeatherRetrievalInProgress = true;
            Intent intent = new Intent(getContext(), WeatherIntentService.class);
            intent.putExtra(WeatherIntentService.LOCATION_DATA_EXTRA, location);
            getActivity().startService(intent);
        }
    }

    @OnClick(R.id.img_weather_icon)
    public void updateWeather() {
        if (isNewDiaryNote && mDiaryNote != null && mDiaryNote.getLocation() != null) {
            startWeatherRetrieval(mDiaryNote.getLocation());
        }
    }

    @Subscribe
    public void getWeather(WeatherInfo weather) {
        if (weather.getWeatherMain() != null) {
            mDiaryNote.setWeather(weather);
            setWeatherViews();
        }
    }

    private void setWeatherViews() {
        if (mDiaryNote.getWeather() != null) {
            WeatherInfo weather;
            weather = mDiaryNote.getWeather();
            Glide.with(DiaryFragment.this)
                    .load(getString(R.string.weather_api_icon_url, weather.getWeatherIcon()))
                    .crossFade()
                    .into(mImgWeatherIcon);
            mTxtWeatherInfo.setText(getString(R.string.weather_api_info_text,
                    weather.getTemp(),
                    weather.getWeatherDescription()));
            mWeatherLayout.setVisibility(View.VISIBLE);
        } else {
            mImgWeatherIcon.setImageBitmap(null);
            mTxtWeatherInfo.setText(R.string.weather_api_na_text);
            mWeatherLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.diary_location_layout)
    public void showMap() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        ObjectAnimator anim = ObjectAnimator.ofFloat(mLocationDropDown, "rotation", 0, 180);
        anim.setDuration(300);

        if (mMapFragment.isHidden()) {
            anim.start();
            fragmentTransaction.show(mMapFragment).commit();
            mContentScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    mContentScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        } else {
            anim.setFloatValues(180, 360);
            anim.start();
            fragmentTransaction.hide(mMapFragment).commit();
        }
    }

    private Marker mMarker = null;
    private MarkerOptions mMarkerOptions = null;

    private void putMarker(LatLng coordinates) {
        mMarkerOptions = new MarkerOptions().position(coordinates);
        putMarker();
    }

    private void putMarker() {
        if (mMap != null && mMarkerOptions != null) {
            if (mMarker == null) {
                mMarker = mMap.addMarker(mMarkerOptions);
            } else {
                mMarker.setPosition(mMarkerOptions.getPosition());
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarkerOptions.getPosition(), 17f));
        }
    }

    private void setupMap() {
        if (mMap == null) {
            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.diary_note_map_fragment);
            mMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        final View mapView = mMapFragment.getView();
        mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                ViewGroup.LayoutParams params = mapView.getLayoutParams();
                params.height = mapView.getWidth();
                mapView.setLayoutParams(params);

                getChildFragmentManager().beginTransaction().hide(mMapFragment).commit();
            }
        });

        putMarker();
    }

    @OnClick(R.id.btn_hide_warning)
    public void hideWarning() {
        if (mWarningCheckbox.isChecked()) {
            mSharedPreferences.edit().putBoolean(Constants.KEY_SHOW_WARNING, false).apply();
        }
        mWarning.setVisibility(View.GONE);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!isTitleVisible) {
                // show collapsed toolbar title
                Utils.startAlphaAnimation(mEdtDiaryNoteTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                // hide expanded toolbar title
                Utils.startAlphaAnimation(mBigTitleLayout, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                // pass text to collapsed toolbar title
                mEdtDiaryNoteTitle.setText(mBigTitle.getText());
                // handle focus
                if (mBigTitle.isFocused()) {
                    int position = mBigTitle.getSelectionStart();
                    mEdtDiaryNoteTitle.requestFocus();
                    mEdtDiaryNoteTitle.setSelection(position);
                }

                mEdtDiaryNoteTitle.setVisibility(View.VISIBLE);
                isTitleVisible = true;
            }
        } else {
            if (isTitleVisible) {
                // hide collapsed toolbar title
                Utils.startAlphaAnimation(mEdtDiaryNoteTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                // show expanded toolbar title
                Utils.startAlphaAnimation(mBigTitleLayout, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                // pass text to expanded toolbar title
                mBigTitle.setText(mEdtDiaryNoteTitle.getText());
                // handle focus
                if (mEdtDiaryNoteTitle.isFocused()) {
                    int position = mEdtDiaryNoteTitle.getSelectionStart();
                    mBigTitle.requestFocus();
                    mBigTitle.setSelection(position);
                }

                mEdtDiaryNoteTitle.setVisibility(View.GONE);
                isTitleVisible = false;
            }
        }
    }


}
