package com.travelersdiary.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.ReminderItem;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListAdapter extends FirebaseRecyclerAdapter<ReminderItem, ReminderListAdapter.ViewHolder> {

    private static MultiSelector multiSelector;

    public ReminderListAdapter(Query ref) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
        this.multiSelector = null;
    }

    public ReminderListAdapter(Firebase ref) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
        this.multiSelector = null;
    }

    public ReminderListAdapter(Query ref, MultiSelector multiSelector) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
        this.multiSelector = multiSelector;
    }

    public ReminderListAdapter(Firebase ref, MultiSelector multiSelector) {
        super(ReminderItem.class, R.layout.list_item_reminder, ReminderListAdapter.ViewHolder.class, ref);
        this.multiSelector = multiSelector;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class ViewHolder extends SwappingHolder {

        @Bind(R.id.item_reminder_todo_item_title_text_view)
        TextView textViewTitle;
        @Bind(R.id.item_reminder_todo_item_remind_info_text_view)
        TextView textViewInfo;
        @Bind(R.id.item_reminder_todo_item_type_icon)
        ImageView imageViewItemTypeIcon;
        @Bind(R.id.item_reminder_completed_icon)
        ImageView imageViewCompletedIcon;

        public ViewHolder(View view) {
            super(view, multiSelector);
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
        if (model.isCompleted()){
            viewHolder.imageViewCompletedIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageViewCompletedIcon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }
}
