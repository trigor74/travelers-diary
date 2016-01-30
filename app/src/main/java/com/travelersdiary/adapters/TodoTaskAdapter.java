package com.travelersdiary.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.models.TodoTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoTaskAdapter extends FirebaseRecyclerAdapter<TodoTask, TodoTaskAdapter.ViewHolder> {

    private boolean mViewAsCheckboxes;

    public void setViewAsCheckboxes(boolean viewAsCheckboxes) {
        this.mViewAsCheckboxes = viewAsCheckboxes;
        notifyDataSetChanged();
    }

    public TodoTaskAdapter(Query ref) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = false;
    }

    public TodoTaskAdapter(Query ref, boolean mViewAsCheckboxes) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = mViewAsCheckboxes;
    }

    public TodoTaskAdapter(Firebase ref) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = false;
    }

    public TodoTaskAdapter(Firebase ref, boolean mViewAsCheckboxes) {
        super(TodoTask.class, R.layout.list_item_task_item, TodoTaskAdapter.ViewHolder.class, ref);
        this.mViewAsCheckboxes = mViewAsCheckboxes;
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
