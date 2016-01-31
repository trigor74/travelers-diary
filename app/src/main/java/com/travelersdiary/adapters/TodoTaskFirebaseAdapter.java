package com.travelersdiary.adapters;

import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.models.TodoTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoTaskFirebaseAdapter extends FirebaseRecyclerAdapter<TodoTask, TodoTaskFirebaseAdapter.ViewHolder> {

    private boolean mViewAsCheckboxes;

    public void setViewAsCheckboxes(boolean viewAsCheckboxes) {
        this.mViewAsCheckboxes = viewAsCheckboxes;
        notifyDataSetChanged();
    }

    private boolean mEditable;

    public void setEditable(boolean editable) {
        this.mEditable = editable;
        notifyDataSetChanged();
    }

    public TodoTaskFirebaseAdapter(Query ref) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskFirebaseAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = false;
        this.mEditable = false;
    }

    public TodoTaskFirebaseAdapter(Query ref, boolean mViewAsCheckboxes) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskFirebaseAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = mViewAsCheckboxes;
        this.mEditable = false;
    }

    public TodoTaskFirebaseAdapter(Firebase ref) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskFirebaseAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = false;
        this.mEditable = false;
    }

    public TodoTaskFirebaseAdapter(Firebase ref, boolean mViewAsCheckboxes) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskFirebaseAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = mViewAsCheckboxes;
        this.mEditable = false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_item_checkbox)
        AppCompatCheckBox checkBox;
        @Bind(R.id.task_item_edit_text)
        EditText editText;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, TodoTask model, int position) {
        if (mEditable) {
            viewHolder.editText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            viewHolder.editText.setFocusable(true);
            viewHolder.editText.setFocusableInTouchMode(true);
        } else {
            viewHolder.editText.setInputType(InputType.TYPE_NULL);
            viewHolder.editText.setFocusable(false);
            viewHolder.editText.setFocusableInTouchMode(false);
        }

        viewHolder.editText.setText(model.getItem());
        viewHolder.checkBox.setChecked(model.isChecked());

        if (mViewAsCheckboxes) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (model.isChecked()) {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }
}
