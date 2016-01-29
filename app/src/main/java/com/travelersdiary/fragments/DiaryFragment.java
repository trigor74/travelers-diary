package com.travelersdiary.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ArrowKeyMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.Travel;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryFragment extends Fragment {

    @Bind(R.id.fab_edit_diary_note)
    FloatingActionButton mFabEditDiaryNote;

    @Bind(R.id.rt_editor)
    RTEditText mRtEditText;

    @Bind(R.id.rte_toolbar_container)
    ViewGroup mToolbarContainer;

    @Bind(R.id.rte_toolbar)
    RTToolbar mRtToolbar;

    @Bind(R.id.txt_date)
    TextView mTxtDate;

    @Bind(R.id.txt_day)
    TextView mTxtDay;

    @Bind(R.id.txt_month_year)
    TextView mTxtMonthYear;

    @Bind(R.id.txt_time)
    TextView mTxtTime;

    @Bind(R.id.txt_travel)
    TextView mTxtTravel;

//    @Bind(R.id.spinner_travels)
//    Spinner mTravelsSpinner;

    private ActionBar mSupportActionBar;

    private EditText mEdtDiaryNoteTitle;

    private boolean isEditingMode;

    private RTManager mRtManager;
    private InputMethodManager mInputMethodManager;

    private Firebase mItemRef;
    private ValueEventListener mValueEventListener;
    private FirebaseListAdapter<Travel> mAdapter;
    private DiaryNote mDiaryNote;

    private String mMessage;
    private String mUserUID;
    private String mKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // read extras
        if (savedInstanceState == null) {
            Intent intent = getActivity().getIntent();
            mMessage = getStringExtra(intent, "message");
        }

        // set theme
        getActivity().setTheme(R.style.RteTheme);

        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        mKey = getArguments().getString(Constants.KEY_DAIRY_NOTE_REF);

        //get toolbar
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                super.populateView(view, travel, position);
                ((TextView) view.findViewById(android.R.id.text1)).setText(travel.getTitle());
            }
        };

        mEdtDiaryNoteTitle = (EditText) (getActivity()).findViewById(R.id.edt_diary_note_title);
        mEdtDiaryNoteTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.HIDE);
                } else {
                    mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW);
                }
            }
        });

//        mTravelsSpinner.setAdapter(mAdapter);

        addDataChangeListener();

        enableReviewingMode();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fab_edit_diary_note)
    public void enableEditingMode() {
        isEditingMode = true;

        // reset edit text field to editable mode
        mRtEditText.setTextIsSelectable(false);
        mRtEditText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        mRtEditText.setCursorVisible(true);
        mRtEditText.setFocusable(true);
        mRtEditText.setFocusableInTouchMode(true);
        mRtEditText.setClickable(true);
        mRtEditText.setLongClickable(true);
        mRtEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mRtEditText.requestFocus();
        mRtEditText.setSelection(mRtEditText.getText().length());

        //setup rte toolbar
        mToolbarContainer.setVisibility(View.VISIBLE);
        mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.SHOW);

        //setup title field
        mEdtDiaryNoteTitle.setVisibility(View.VISIBLE);
        mEdtDiaryNoteTitle.setText(mSupportActionBar.getTitle());
        mSupportActionBar.setDisplayShowTitleEnabled(false);

        //show spinner, hide travel title
//        mTravelsSpinner.setVisibility(View.VISIBLE);
        mTxtTravel.setVisibility(View.GONE);

        //show keyboard
        mInputMethodManager.showSoftInput(mRtEditText, InputMethodManager.SHOW_IMPLICIT);

        //hide fab
        mFabEditDiaryNote.setVisibility(View.GONE);

        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void enableReviewingMode() {
        isEditingMode = false;

        retrieveData();

        // make edit text field not editable
//        mRtEditText.setTextIsSelectable(true);
//        mRtEditText.setInputType(InputType.TYPE_NULL);
        mRtEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mRtEditText.setFocusable(false);

        //setup rte toolbar
        mToolbarContainer.setVisibility(View.GONE);
        mRtManager.setToolbarVisibility(RTManager.ToolbarVisibility.HIDE);

        //setup title field
        mEdtDiaryNoteTitle.setVisibility(View.GONE);
        mSupportActionBar.setDisplayShowTitleEnabled(true);

        //show travel title, hide spinner
        mTxtTravel.setVisibility(View.VISIBLE);
//        mTxtTravel.setText("sample");
//        mTravelsSpinner.setVisibility(View.GONE);

        //hide keyboard
        mInputMethodManager.hideSoftInputFromWindow(mRtEditText.getWindowToken(), 0);

        //show fab
        mFabEditDiaryNote.setVisibility(View.VISIBLE);

        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void addDataChangeListener() {
        mItemRef = new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                .child(mKey);

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDiaryNote = dataSnapshot.getValue(DiaryNote.class);
                mSupportActionBar.setTitle(mDiaryNote.getTitle());
                mRtEditText.setRichTextEditing(true, mDiaryNote.getText());
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
                        mSupportActionBar.setTitle(mDiaryNote.getTitle());

                        Date time = new Date(mDiaryNote.getTime());
                        mTxtDate.setText(new SimpleDateFormat("dd").format(time));
                        mTxtDay.setText(new SimpleDateFormat("EEE").format(time));
                        mTxtMonthYear.setText(new SimpleDateFormat("MMM, yyyy").format(time));

                        mTxtTravel.setText(mDiaryNote.getTravelTitle());

                        mRtEditText.setRichTextEditing(true, mDiaryNote.getText());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getStringExtra(Intent intent, String key) {
        String s = intent.getStringExtra(key);
        return s == null ? "" : s;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRtManager.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRtManager != null) {
            mRtManager.onDestroy(true);
        }

        if (mItemRef != null) {
            mItemRef.removeEventListener(mValueEventListener);
        }

        mAdapter.cleanup();
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
            menu.getItem(0).setIcon(R.drawable.ic_save_white_24dp);
        } else {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            menu.getItem(0).setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isEditingMode) {
                    if (mRtEditText.hasChanged()) {
                        showDiscardDialog();
                    } else {
                        enableReviewingMode();
                    }
                } else {
                    getActivity().finish();
                }
                return true;
            case R.id.action_save:
                saveChanges();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveChanges() {
        if (!isEmpty(mEdtDiaryNoteTitle)) {
            mDiaryNote.setTitle(mEdtDiaryNoteTitle.getText().toString());
        } else {
            Toast.makeText(getContext(), "Title field is empty", Toast.LENGTH_SHORT).show();
            mEdtDiaryNoteTitle.requestFocus();
            return;
        }

        mDiaryNote.setText(mRtEditText.getText(RTFormat.HTML));
        mItemRef.setValue(mDiaryNote);

        mRtEditText.resetHasChanged();
        Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();

        enableReviewingMode();
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }


}
