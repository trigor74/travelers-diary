package com.travelersdiary.screens.reminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.FirebaseMultiSelectRecyclerAdapter;
import com.travelersdiary.interfaces.OnItemClickListener;
import com.travelersdiary.models.ReminderItem;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListFragment extends Fragment {

    @Bind(R.id.reminder_list)
    RecyclerView mReminderList;

    @Bind(R.id.empty_view)
    LinearLayout mEmptyView;

    private static FirebaseMultiSelectRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ValueEventListener eventListener;
    private Query query;

    private static Context mContext;
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
                for (int key :
                        (List<Integer>) mAdapter.getSelectedItems()) {
                    ReminderItem reminderItem = (ReminderItem) mAdapter.getItem(key);

                    Utils.disableAlarmGeofence(mContext, reminderItem);
                    Firebase ref = mAdapter.getRef(key);
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

        mContext = getActivity().getApplicationContext();

        // LayoutManager
        mLayoutManager = new LinearLayoutManager(getContext());
        mReminderList.setLayoutManager(mLayoutManager);

        // animation
        mReminderList.setItemAnimator(new DefaultItemAnimator());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));

        String travelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);
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
            protected void populateViewHolder(final ReminderListFragment.ViewHolder holder, final ReminderItem model, final int position) {

                holder.completedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(Constants.FIREBASE_REMINDER_COMPLETED, isChecked);
                        getRef(position).updateChildren(map);

                        if (isChecked) {
                            Utils.disableAlarmGeofence(getContext(), model);
                            holder.title.setPaintFlags(holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            Utils.enableAlarmGeofence(getContext(), model, getRef(position).getKey());
                            holder.title.setPaintFlags(holder.title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    }
                });

                holder.title.setText(model.getTitle());
                String type = model.getType();
                if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(type)) {
                    // remind at time
                    long time = model.getTime();
                    String date = SimpleDateFormat.getDateInstance().format(time);
                    String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(time);
                    holder.info.setText(String.format("%s %s", date, timeText));
                    holder.typeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
                } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
                    // remind at location
                    holder.info.setText(model.getWaypoint().getTitle());
                    holder.typeIcon.setImageResource(R.drawable.ic_location_on_black_24dp);
                } else {
                    // don't remind
                    holder.info.setText(R.string.reminder_dont_remind_text);
                    holder.typeIcon.setImageResource(R.drawable.ic_alarm_off_black_24dp);
                }

                holder.completedCheckBox.setChecked(model.isCompleted());

                // card view selection
                holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
            }
        };

        mReminderList.setAdapter(mAdapter);

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() == 0) {
                    mReminderList.setVisibility(View.GONE);
                    TextView emptyText = (TextView) mEmptyView.findViewById(R.id.empty_view_text);
                    emptyText.setText(getString(R.string.empty_reminder));
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    mReminderList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };

        query.addValueEventListener(eventListener);
    }

    @Override
    public void onPause() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        query.removeEventListener(eventListener);
        super.onDestroyView();
    }


    private static OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!mAdapter.tapSelection(position)) {
                String key = mAdapter.getRef(position).getKey();

                Intent intent = new Intent(view.getContext(), ReminderItemActivity.class);
                intent.putExtra(Constants.KEY_REMINDER_ITEM_KEY, key);
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
                mAdapter.tapSelection(position);
                if (mAdapter.getSelectedItemCount() == 0) {
                    mDeleteMode.finish();
                } else {
                    int selectedItems = mAdapter.getSelectedItemCount();
                    int items = mAdapter.getItemCount();
                    mDeleteMode.setTitle(view.getContext().getString(R.string.diary_list_action_mode_title_text, selectedItems, items));
                }
            }
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.reminder_selected_overlay)
        RelativeLayout selectedOverlay;
        @Bind(R.id.reminder_card_view)
        CardView cardView;
        @Bind(R.id.reminder_title)
        TextView title;
        @Bind(R.id.reminder_info)
        TextView info;
        @Bind(R.id.reminder_type_icon)
        ImageView typeIcon;
        @Bind(R.id.reminder_completed_checkbox)
        AppCompatCheckBox completedCheckBox;

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
