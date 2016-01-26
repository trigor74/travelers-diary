package com.travelersdiary.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.ReminderListAdapter;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListFragment extends Fragment {

    @Bind(R.id.reminder_list)
    RecyclerView mReminderList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mReminderList.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayoutManager = new LinearLayoutManager(getContext());
        mReminderList.setLayoutManager(mLayoutManager);

        // animation
        mReminderList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mReminderList.addItemDecoration(itemDecoration);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));
        Query query = mFirebaseRef.orderByKey();

        mAdapter = new ReminderListAdapter(query);
        mReminderList.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }
}
