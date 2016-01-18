package com.travelersdiary.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.DiaryNote;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiaryFragment extends Fragment {

    @Bind(R.id.diary_note_web_view)
    WebView mWebView;

    private String mUserUID;
    private String mKey;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        mKey = getArguments().getString(Constants.KEY_DAIRY_NOTE_REF);
        retrieveData(mKey);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.test_button)
    public void opedEditor() {
        DiaryEditorFragment diaryEditorFragment = DiaryEditorFragment.getInstance();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_DAIRY_NOTE_REF, mKey);
        diaryEditorFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, diaryEditorFragment)
                .addToBackStack(null)
                .commit();
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
                mWebView.loadDataWithBaseURL("", diaryNote.getText(), "text/html", "UTF-8", "");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
