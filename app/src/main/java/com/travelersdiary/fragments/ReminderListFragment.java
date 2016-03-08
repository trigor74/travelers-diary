package com.travelersdiary.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.adapters.ReminderListAdapter;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import java.lang.reflect.Field;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListFragment extends Fragment {

    @Bind(R.id.reminder_list)
    RecyclerView mReminderList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static final String MULTISELECTOR_STATE_TAG = "multiselector";

    private MultiSelector mMultiSelector = new MultiSelector();

    private ModalMultiSelectorCallback mDeleteMode = new ModalMultiSelectorCallback(mMultiSelector) {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            actionMode.getMenuInflater().inflate(R.menu.reminder_list_item_context, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_item_delete) {
                mode.finish();

                for (Integer position : mMultiSelector.getSelectedPositions()) {
                    Firebase itemRef = mAdapter.getRef(position);
                    itemRef.removeValue();
//                    mAdapter.notifyItemRemoved(position);
                }

                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mMultiSelector.clearSelections();
            try {
                Field field = mMultiSelector.getClass().getDeclaredField("mIsSelectable");
                if (field != null) {
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    field.set(mMultiSelector, false);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (mMultiSelector != null) {
            Bundle bundle = savedInstanceState;
            if (bundle != null) {
                mMultiSelector.restoreSelectionStates(bundle.getBundle(MULTISELECTOR_STATE_TAG));
            }

            if (mMultiSelector.isSelectable()) {
                if (mDeleteMode != null) {
                    mDeleteMode.setClearOnPrepare(false);
                    ((AppCompatActivity) getActivity()).startSupportActionMode(mDeleteMode);
                }
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

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

        // LayoutManager
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
        Query query;

        String travelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_KEY);
        if (travelId != null && !travelId.isEmpty()) {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_REMINDER_TRAVELID).equalTo(travelId);
        } else {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_REMINDER_ACTIVE).equalTo(true);
        }

        mAdapter = new ReminderListAdapter(query, mMultiSelector);
        mReminderList.setAdapter(mAdapter);

        ((ReminderListAdapter) mAdapter).setOnItemClickListener(new ReminderListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!mMultiSelector.tapSelection(position, mAdapter.getItemId(position))) {
                    String key = mAdapter.getRef(position).getKey();

                    Intent intent = new Intent(getActivity(), ReminderItemActivity.class);
                    intent.putExtra(Constants.KEY_REMINDER_ITEM_REF, key);
                    startActivity(intent);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!mMultiSelector.isSelectable()) {
                    ((AppCompatActivity) getActivity()).startSupportActionMode(mDeleteMode);
                    mMultiSelector.setSelectable(true);
                    mMultiSelector.setSelected(position, mAdapter.getItemId(position), true);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(MULTISELECTOR_STATE_TAG, mMultiSelector.saveSelectionStates());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }
}
