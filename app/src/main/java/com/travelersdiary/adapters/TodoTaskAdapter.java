package com.travelersdiary.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.travelersdiary.R;
import com.travelersdiary.models.TodoTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoTaskAdapter extends RecyclerView.Adapter<TodoTaskAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnTaskCompletedListener {
        void onTaskCompleted(boolean completed);
    }

    private static OnTaskCompletedListener onTaskCompletedListener;

    public void setOnTaskCompletedListener(OnTaskCompletedListener onTaskCompletedListener) {
        this.onTaskCompletedListener = onTaskCompletedListener;
    }

    private boolean mCompleted;

    public void setCompleted(boolean completed) {
        this.mCompleted = completed;
        for (int i = 0; i < mTodoTaskItemList.size(); i++) {
            mTodoTaskItemList.get(i).setChecked(completed);
        }
        notifyDataSetChanged();
    }

    public boolean isCompleted() {
        return this.mCompleted;
    }

    private void checkCompleted() {
        if (mViewAsCheckboxes) {
            boolean completed = true;
            for (int i = 0; i < mTodoTaskItemList.size(); i++) {
                if (!mTodoTaskItemList.get(i).isChecked()) {
                    completed = false;
                }
            }
            if (mCompleted != completed) {
                if (onTaskCompletedListener != null) {
                    onTaskCompletedListener.onTaskCompleted(completed);
                    this.mCompleted = completed;
                }
            }
        }
    }

    private boolean mViewAsCheckboxes;

    public void setViewAsCheckboxes(boolean viewAsCheckboxes) {
        this.mViewAsCheckboxes = viewAsCheckboxes;
        checkCompleted();
        notifyDataSetChanged();
    }

    private int mSelectedItem = -1;

    public void setSelectedItem(int position) {
        mSelectedItem = position;
    }

    private int mEditTextCursorPosition = 0;

    private void setEditTextCursorPosition(int position) {
        mEditTextCursorPosition = position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_item_checkbox)
        AppCompatCheckBox checkBox;
        @Bind(R.id.task_item_edit_text)
        EditText editText;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    setEditTextCursorPosition(editText.getSelectionStart());
                    if (mSelectedItem != position) {
                        setSelectedItem(position);
//                        setEditTextCursorPosition(0);
                        setEditTextCursorPosition(mTodoTaskItemList.get(position).getItem().length());
                    }
                    mTodoTaskItemList.get(position).setChecked(((AppCompatCheckBox) v).isChecked());
                    notifyItemChanged(position);
                    checkCompleted();
                }
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mTodoTaskItemList.get(getLayoutPosition()).setItem(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL
                            && event.getAction() == KeyEvent.ACTION_DOWN) {
                        int cursorPosition = editText.getSelectionStart();
                        if (cursorPosition == 0) {
                            int index = getLayoutPosition();
                            String enteredText = mTodoTaskItemList.get(index).getItem();
                            if (index > 0) {
                                boolean previousCheckState = mTodoTaskItemList.get(index - 1).isChecked();
                                String previousText = mTodoTaskItemList.get(index - 1).getItem();
                                mTodoTaskItemList.get(index - 1).setItem(previousText.concat(enteredText));
                                mTodoTaskItemList.get(index - 1).setChecked(previousCheckState);
                                mTodoTaskItemList.remove(index);
                                notifyItemRemoved(index);
                                notifyItemChanged(index - 1);
                                setSelectedItem(index - 1);
                                setEditTextCursorPosition(previousText.length());
                                checkCompleted();
                                return true;
                            } else {
                                if (enteredText.length() == 0) {
                                    mTodoTaskItemList.get(0).setChecked(false);
                                    notifyItemChanged(0);
                                    setSelectedItem(0);
                                    setEditTextCursorPosition(0);
                                    checkCompleted();
                                    return true;
                                }
                            }
                        }
                    }
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                        int cursorPosition = editText.getSelectionStart();
                        int index = getLayoutPosition();

                        String enteredText = mTodoTaskItemList.get(index).getItem();
                        String startToCursor = enteredText.substring(0, cursorPosition);
                        String cursorToEnd = enteredText.substring(cursorPosition, enteredText.length());
                        mTodoTaskItemList.get(index).setItem(startToCursor);
                        mTodoTaskItemList.add(index + 1, new TodoTask(cursorToEnd, false));
                        notifyItemInserted(index + 1);
                        notifyItemRangeChanged(index, 2);
                        setSelectedItem(index + 1);
                        setEditTextCursorPosition(0);
                        checkCompleted();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private ArrayList<TodoTask> mTodoTaskItemList;

    public ArrayList<TodoTask> getTodoTaskList() {
        return mTodoTaskItemList;
    }

    public void addItem(String line, boolean checked) {
        TodoTask todoTask = new TodoTask(line, checked);
        mTodoTaskItemList.add(todoTask);
        notifyItemInserted(mTodoTaskItemList.size() - 1);
    }

    public void addItem(int index, String line, boolean checked) {
        TodoTask todoTask = new TodoTask(line, checked);
        mTodoTaskItemList.add(index, todoTask);
        notifyItemInserted(index);
    }

    public void remove(int index) {
        mTodoTaskItemList.remove(index);
        notifyItemRemoved(index);
        notifyItemRangeChanged(index, mTodoTaskItemList.size());
    }

    public TodoTaskAdapter(ArrayList<TodoTask> itemList) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = false;
    }

    public TodoTaskAdapter(ArrayList<TodoTask> itemList, boolean viewAsCheckboxes) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = viewAsCheckboxes;
    }

    private Context mContext;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_task_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        TodoTask model = mTodoTaskItemList.get(position);

        viewHolder.editText.setText(model.getItem());
        viewHolder.checkBox.setChecked(model.isChecked());

        if (mViewAsCheckboxes) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            if (model.isChecked() || mCompleted) {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
            if (mCompleted) {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                viewHolder.editText.setPaintFlags(viewHolder.editText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        if (mSelectedItem == position) {
            viewHolder.editText.setSelected(true);
            viewHolder.editText.requestFocus();
            if (mEditTextCursorPosition > viewHolder.editText.getText().length()) {
                setEditTextCursorPosition(viewHolder.editText.getText().length());
            }
            viewHolder.editText.setSelection(mEditTextCursorPosition);
        } else {
            viewHolder.editText.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return mTodoTaskItemList.size();
    }
}
