package com.travelersdiary.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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
                    if (((AppCompatCheckBox) v).isChecked()) {
                        mTodoTaskItemList.get(getLayoutPosition()).setChecked(true);
                        notifyItemChanged(getLayoutPosition());
                    } else {
                        mTodoTaskItemList.get(getLayoutPosition()).setChecked(false);
                        notifyItemChanged(getLayoutPosition());
                    }
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
                    if(keyCode == KeyEvent.KEYCODE_DEL){
                        //this is for backspace
                        int cursorPosition = editText.getSelectionStart();
                        Toast.makeText(mContext, "DELETE KEY, CURSOR AT " + Integer.toString(cursorPosition), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        int cursorPosition = editText.getSelectionStart();

                        //select all the text after the cursor
                        CharSequence enteredText = mTodoTaskItemList.get(getLayoutPosition()).getItem();
                        CharSequence cursorToEnd = enteredText.subSequence(cursorPosition, enteredText.length());

                        Toast.makeText(mContext, "ENTER KEY, CURSOR AT " + Integer.toString(cursorPosition) + ", TEXT: " + cursorToEnd.toString(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private ArrayList<TodoTask> mTodoTaskItemList;

    public ArrayList<TodoTask> getTodoTask() {
        return mTodoTaskItemList;
    }

    public TodoTaskAdapter(ArrayList<TodoTask> itemList) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = false;
        this.mEditable = false;
    }

    public TodoTaskAdapter(ArrayList<TodoTask> itemList, boolean viewAsCheckboxes) {
        this.mTodoTaskItemList = itemList;
        this.mViewAsCheckboxes = viewAsCheckboxes;
        this.mEditable = false;
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

        if (mEditable) {
//            viewHolder.editText.setInputType(InputType.TYPE_CLASS_TEXT
//                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE
//                    | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            viewHolder.editText.setFocusable(true);
            viewHolder.editText.setFocusableInTouchMode(true);
            viewHolder.editText.setLongClickable(true);
//            viewHolder.editText.setKeyListener(new EditText(mContext.getApplicationContext()).getKeyListener());
        } else {
//            viewHolder.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            viewHolder.editText.setFocusable(false);
            viewHolder.editText.setFocusableInTouchMode(false);
            viewHolder.editText.setLongClickable(false);
//            viewHolder.editText.setKeyListener(null);
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

    @Override
    public int getItemCount() {
        return mTodoTaskItemList.size();
    }
}
