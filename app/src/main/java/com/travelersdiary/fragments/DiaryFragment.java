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
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.onegravity.rteditor.RTEditText;
import com.onegravity.rteditor.RTManager;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.DiaryNote;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryFragment extends Fragment {

    @Bind(R.id.fab_edit_diary_note)
    FloatingActionButton mFabEditDiaryNote;

    @Bind(R.id.rtEditText)
    RTEditText mRtEditText;

//    @Bind(R.id.rte_toolbar_container)
//    ViewGroup mToolbarContainer;
//
//    @Bind(R.id.rte_toolbar)
//    RTToolbar mRtToolbar;

    private ActionBar mSupportActionBar;

    private boolean isEditingMode;

    private RTManager mRtManager;
    private InputMethodManager mInputMethodManager;

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

        // register toolbar (if it exists)
//        if (mRtToolbar != null) {
//            mRtManager.registerToolbar(mToolbarContainer, mRtToolbar);
//        }

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // register rich text editor
        mRtManager.registerEditor(mRtEditText, true);

        retrieveData(mKey);

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

        mRtEditText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        mRtEditText.requestFocus();

        mInputMethodManager.showSoftInput(mRtEditText, InputMethodManager.SHOW_IMPLICIT);

        mFabEditDiaryNote.setVisibility(View.GONE);

        mSupportActionBar.invalidateOptionsMenu();
    }

    private void enableViewingMode() {
        isEditingMode = false;
        // make edit text field not editable
        mRtEditText.setTextIsSelectable(true);
        mRtEditText.setInputType(InputType.TYPE_NULL);

        mInputMethodManager.hideSoftInputFromWindow(mRtEditText.getWindowToken(), 0);

        mFabEditDiaryNote.setVisibility(View.VISIBLE);

        mSupportActionBar.invalidateOptionsMenu();

    }

    private void retrieveData(String key) {
        Firebase itemRef = new Firebase(Constants.FIREBASE_URL)
                .child("users")
                .child(mUserUID)
                .child("diary")
                .child(key);
        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DiaryNote diaryNote = dataSnapshot.getValue(DiaryNote.class);
                mRtEditText.setRichTextEditing(true, diaryNote.getText());
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
                        // go to view
                        enableViewingMode();
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
                        enableViewingMode();
                    }
                } else {
                    getActivity().finish();
                }
                return true;
            case R.id.action_save:
                String text = mRtEditText.getText(RTFormat.HTML);
                Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
                mRtEditText.resetHasChanged();
                enableViewingMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
