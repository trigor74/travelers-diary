package com.travelersdiary.fragments;

import android.content.Intent;
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
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.adapters.DiaryListAdapter;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryListFragment extends Fragment {

    @Bind(R.id.diary_list)
    RecyclerView mDiaryList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mDiaryList.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayoutManager = new LinearLayoutManager(getContext());
        mDiaryList.setLayoutManager(mLayoutManager);

        // animation
        mDiaryList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mDiaryList.addItemDecoration(itemDecoration);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
        Query query;

        String travelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_KEY);
        if (travelId != null && !travelId.isEmpty()) {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(travelId);
        } else {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_DIARY_TIME);
        }

        if (mAdapter != null) {
            mAdapter.cleanup();
            mAdapter = new DiaryListAdapter(query);
            mDiaryList.swapAdapter(mAdapter, true);
        } else {
            mAdapter = new DiaryListAdapter(query);
            mDiaryList.setAdapter(mAdapter);
        }

        ((DiaryListAdapter) mAdapter).setOnItemClickListener(new DiaryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String key = mAdapter.getRef(position).getKey();

                Intent intent = new Intent(getActivity(), DiaryActivity.class);
                intent.putExtra(Constants.KEY_DAIRY_NOTE_REF, key);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }

}
