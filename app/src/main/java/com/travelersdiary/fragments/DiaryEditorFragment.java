package com.travelersdiary.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.onegravity.rteditor.RTToolbar;
import com.onegravity.rteditor.api.RTApi;
import com.onegravity.rteditor.api.RTMediaFactoryImpl;
import com.onegravity.rteditor.api.RTProxyImpl;
import com.onegravity.rteditor.api.format.RTFormat;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.DiaryNote;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryEditorFragment extends Fragment {

    @Bind(R.id.rtEditText)
    RTEditText mRtEditText;

    @Bind(R.id.rte_toolbar_container)
    ViewGroup toolbarContainer;

    @Bind(R.id.rte_toolbar)
    RTToolbar rtToolbar;

    private RTManager mRtManager;

    private String mMessage;
    private String mUserUID;

    public static DiaryEditorFragment getInstance() {
        return new DiaryEditorFragment();
    }

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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        String key = getArguments().getString(Constants.KEY_DAIRY_NOTE_REF);
        retrieveData(key);

        // set theme
        getActivity().setTheme(R.style.RteTheme);

        // inflate layout
        View view = inflater.inflate(R.layout.fragment_diary_editor, container, false);
        ButterKnife.bind(this, view);

        //get toolbar
        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        // create RTManager
        RTApi rtApi = new RTApi(getContext(), new RTProxyImpl(getActivity()), new RTMediaFactoryImpl(getContext(), true));
        mRtManager = new RTManager(rtApi, savedInstanceState);

        // register toolbar (if it exists)
        if (rtToolbar != null) {
            mRtManager.registerToolbar(toolbarContainer, rtToolbar);
        }

        // register rich text editor
        mRtManager.registerEditor(mRtEditText, true);
        if (mMessage != null) {
            mRtEditText.setRichTextEditing(true, mMessage);
            mRtEditText.resetHasChanged();
        }

        mRtEditText.requestFocus();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mRtEditText, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mRtEditText.getWindowToken(), 0);
    }

    private String getStringExtra(Intent intent, String key) {
        String s = intent.getStringExtra(key);
        return s == null ? "" : s;
    }

    private void retrieveData(String key) {
        Firebase itemRef = new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRtManager.onSaveInstanceState(outState);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.diary_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mRtEditText.hasChanged()) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.discard_changes_text)
                            .setPositiveButton(R.string.discard, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //do nothing
                                }
                            })
                            .show();
                } else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                return true;
            case R.id.action_save:
                String text = mRtEditText.getText(RTFormat.HTML);
                Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();
                mRtEditText.resetHasChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
