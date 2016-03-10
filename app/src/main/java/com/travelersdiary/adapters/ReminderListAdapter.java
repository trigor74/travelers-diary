package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.ReminderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListAdapter extends FirebaseRecyclerAdapter<ReminderItem, ReminderListAdapter.ViewHolder> {

    public ReminderListAdapter(Query ref) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
    }

    public ReminderListAdapter(Firebase ref) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

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
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getLayoutPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        if (isSelectable()) {
            viewHolder.itemView.setActivated(isSelected(position));
        } else {
            viewHolder.itemView.setActivated(false);
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, ReminderItem model, int position) {
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

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private HashMap<Integer, Firebase> mSelectedItemsRef = new HashMap<>();
    private boolean mSelectable;

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelected(int position, boolean checked) {
        if (checked) {
            mSelectedItems.put(position, true);
            mSelectedItemsRef.put(position, getRef(position));
        } else {
            mSelectedItems.delete(position);
            mSelectedItemsRef.remove(position);
        }
        notifyItemChanged(position);
    }

    public boolean tapSelection(int position) {
        if (isSelectable()) {
            if (!mSelectedItems.get(position, false)) {
                mSelectedItems.put(position, true);
                mSelectedItemsRef.put(position, getRef(position));
            } else {
                mSelectedItems.delete(position);
                mSelectedItemsRef.remove(position);
            }
            notifyItemChanged(position);
            return true;
        } else {
            return false;
        }
    }

    public boolean isSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clearSelections() {
        mSelectedItems.clear();
        mSelectedItemsRef.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            if (mSelectedItems.valueAt(i)) {
                items.add(mSelectedItems.keyAt(i));
            }
        }
        return items;
    }

    public List<Firebase> getSelectedItemsRef() {
        List<Firebase> items = new ArrayList<>(mSelectedItemsRef.size());
        items.addAll(mSelectedItemsRef.values());
        return items;
    }
}
