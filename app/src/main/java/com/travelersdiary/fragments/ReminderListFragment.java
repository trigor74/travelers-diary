package com.travelersdiary.fragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.adapters.ReminderListAdapter;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListFragment extends Fragment {

    @Bind(R.id.reminder_list)
    RecyclerView mReminderList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ActionMode mDeleteMode = null;
    private ActionMode.Callback mDeleteModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.reminder_list_item_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_item_delete) {
                for (Firebase ref :
                        ((ReminderListAdapter) mAdapter).getSelectedItemsRef()) {
                    Log.v("SELECTOR", "Selected ref:" + ref.toString());
                    ref.removeValue();
                }
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ((ReminderListAdapter) mAdapter).setSelectable(false);
            ((ReminderListAdapter) mAdapter).clearSelections();
            mDeleteMode = null;
        }
    };

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

        mAdapter = new ReminderListAdapter(query);
        mReminderList.setAdapter(mAdapter);

        ((ReminderListAdapter) mAdapter).setOnItemClickListener(new ReminderListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!((ReminderListAdapter) mAdapter).tapSelection(position)) {
                    String key = mAdapter.getRef(position).getKey();

                    Intent intent = new Intent(getActivity(), ReminderItemActivity.class);
                    intent.putExtra(Constants.KEY_REMINDER_ITEM_REF, key);
                    startActivity(intent);
                } else {
                    if (mDeleteMode != null) {
                        if (((ReminderListAdapter) mAdapter).getSelectedItemCount() == 0) {
                            mDeleteMode.finish();
                        } else {
                            int selectedItems = ((ReminderListAdapter) mAdapter).getSelectedItemCount();
                            int items = ((ReminderListAdapter) mAdapter).getItemCount();
                            mDeleteMode.setTitle(getString(R.string.reminder_list_action_mode_title_text, selectedItems, items));
                        }
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (mDeleteMode == null) {
                    mDeleteMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mDeleteModeCallback);
                }
                if (mDeleteMode != null) {
                    ((ReminderListAdapter) mAdapter).setSelectable(true);
                    ((ReminderListAdapter) mAdapter).setSelected(position, true);

                    int selectedItems = ((ReminderListAdapter) mAdapter).getSelectedItemCount();
                    int items = ((ReminderListAdapter) mAdapter).getItemCount();
                    mDeleteMode.setTitle(getString(R.string.reminder_list_action_mode_title_text, selectedItems, items));
                }
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
