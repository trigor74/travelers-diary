package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.TodoItem;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ReminderListAdapter extends FirebaseRecyclerAdapter<TodoItem, ReminderListAdapter.ViewHolder> {

    public ReminderListAdapter(Query ref) {
        super(TodoItem.class, R.layout.list_item_reminder_todo_item, ReminderListAdapter.ViewHolder.class, ref);
    }

    public ReminderListAdapter(Firebase ref) {
        super(TodoItem.class, R.layout.list_item_reminder_todo_item, ReminderListAdapter.ViewHolder.class, ref);
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
    protected void populateViewHolder(ViewHolder viewHolder, TodoItem model, int position) {
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
    }
}
