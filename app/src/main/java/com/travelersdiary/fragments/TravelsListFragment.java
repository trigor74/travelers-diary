package com.travelersdiary.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.activities.TravelActivity;
import com.travelersdiary.adapters.TravelsListAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TravelsListFragment extends Fragment {

    @Bind(R.id.travels_list)
    RecyclerView mTravelsList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travels_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mTravelsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayoutManager = new LinearLayoutManager(getContext());
        mTravelsList.setLayoutManager(mLayoutManager);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase mFirebaseRef = new Firebase(Constants.FIREBASE_URL)
                .child("users")
                .child(userUID)
                .child("travels");

        mAdapter = new TravelsListAdapter(mFirebaseRef);
        mTravelsList.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        mAdapter.cleanup();
    }

    @OnClick(R.id.test_button)
    public void onTestButtonClick() {
        startActivity(new Intent(getActivity(), TravelActivity.class));
    }

}
