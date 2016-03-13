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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.adapters.FirebaseMultiSelectRecyclerAdapter;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.interfaces.IOnItemClickListener;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListFragment extends Fragment implements IActionModeFinishCallback {

    @Bind(R.id.reminder_list)
    RecyclerView mReminderList;

    private static FirebaseMultiSelectRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static ActionMode mDeleteMode = null;
    private static ActionMode.Callback mDeleteModeCallback = new ActionMode.Callback() {
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
                        (List<Firebase>) mAdapter.getSelectedItemsRef()) {
                    ref.removeValue();
                }
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.setSelectable(false);
            mAdapter.clearSelections();
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

        mAdapter = new FirebaseMultiSelectRecyclerAdapter<ReminderItem, ReminderListFragment.ViewHolder>(
                ReminderItem.class,
                R.layout.list_item_reminder,
                ReminderListFragment.ViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(ReminderListFragment.ViewHolder viewHolder, ReminderItem model, int position) {

                viewHolder.textViewTitle.setText(model.getTitle());
                String type = model.getType();
                if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(type)) {
                    // remind at time
                    long time = model.getTime();
                    String timeText = SimpleDateFormat.getDateTimeInstance().format(time);
                    viewHolder.textViewInfo.setText(timeText);
                    viewHolder.imageViewItemTypeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
                } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
                    // remind at location
                    viewHolder.textViewInfo.setText(model.getWaypoint().getTitle());
                    viewHolder.imageViewItemTypeIcon.setImageResource(R.drawable.ic_location_on_black_24dp);
                } else {
                    // don't remind
                    viewHolder.textViewInfo.setText(R.string.reminder_dont_remind_text);
                    viewHolder.imageViewItemTypeIcon.setImageResource(R.drawable.ic_alarm_off_black_24dp);
                }
                if (model.isCompleted()) {
                    viewHolder.imageViewCompletedIcon.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imageViewCompletedIcon.setVisibility(View.INVISIBLE);
                }
            }
        };

        mReminderList.setAdapter(mAdapter);
    }

    @Override
    public void finishActionMode() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
        }
    }

    @Override
    public void onPause() {
        finishActionMode();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }


    private static IOnItemClickListener onItemClickListener = new IOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!mAdapter.tapSelection(position)) {
                String key = mAdapter.getRef(position).getKey();

                Intent intent = new Intent(view.getContext(), ReminderItemActivity.class);
                intent.putExtra(Constants.KEY_REMINDER_ITEM_REF, key);
                view.getContext().startActivity(intent);
            } else {
                if (mDeleteMode != null) {
                    if (mAdapter.getSelectedItemCount() == 0) {
                        mDeleteMode.finish();
                    } else {
                        int selectedItems = mAdapter.getSelectedItemCount();
                        int items = mAdapter.getItemCount();
                        mDeleteMode.setTitle(view.getContext().getString(R.string.reminder_list_action_mode_title_text, selectedItems, items));
                    }
                }
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            if (mDeleteMode == null) {
                mDeleteMode = ((AppCompatActivity) view.getContext()).startSupportActionMode(mDeleteModeCallback);
            }
            if (mDeleteMode != null) {
                mAdapter.setSelectable(true);
                mAdapter.setSelected(position, true);

                int selectedItems = mAdapter.getSelectedItemCount();
                int items = mAdapter.getItemCount();
                mDeleteMode.setTitle(view.getContext().getString(R.string.reminder_list_action_mode_title_text, selectedItems, items));
            }
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_reminder_todo_item_title_text_view)
        TextView textViewTitle;
        @Bind(R.id.item_reminder_todo_item_remind_info_text_view)
        TextView textViewInfo;
        @Bind(R.id.item_reminder_todo_item_type_icon)
        ImageView imageViewItemTypeIcon;
        @Bind(R.id.item_reminder_completed_icon)
        ImageView imageViewCompletedIcon;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });

            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
