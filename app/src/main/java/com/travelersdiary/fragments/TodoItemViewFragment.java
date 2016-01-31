package com.travelersdiary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.method.ArrowKeyMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.TodoTaskAdapter;
import com.travelersdiary.adapters.TodoTaskFirebaseAdapter;
import com.travelersdiary.models.TodoItem;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TodoItemViewFragment extends Fragment {
    private ActionBar mSupportActionBar;
    private Firebase mItemRef;
    private TodoItem mTodoItem;

    private String mUserUID;
    private String mKey;

    @Bind(R.id.todo_item_remind_info_text_view)
    TextView textViewInfo;
    @Bind(R.id.todo_item_type_icon)
    ImageView imageViewItemTypeIcon;

    private Context mContext;

    @Bind(R.id.todo_item_task)
    RecyclerView mTodoItemTask;

    private RecyclerView.Adapter mAdapter;

    private InputMethodManager mInputMethodManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_item_view, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        mKey = getArguments().getString(Constants.KEY_TODO_ITEM_REF);

        //get toolbar
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        retrieveData(mKey);

        return view;
    }

    private void retrieveData(String key) {
        mItemRef = new Firebase(Utils.getFirebaseUserReminderUrl(mUserUID))
                .child(key);
        mItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTodoItem = dataSnapshot.getValue(TodoItem.class);
                mSupportActionBar.setTitle(mTodoItem.getTitle());

//                Firebase itemRef = dataSnapshot.getRef();
//                Query todoTaskList = itemRef.child(Constants.FIREBASE_REMINDER_TASK).orderByKey();
//                mAdapter = new TodoTaskFirebaseAdapter(todoTaskList, mTodoItem.isViewAsCheckboxes());

                mAdapter = new TodoTaskAdapter(mTodoItem.getTask(), mTodoItem.isViewAsCheckboxes());

                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mTodoItemTask.setLayoutManager(layoutManager);
                mTodoItemTask.setAdapter(mAdapter);

                long time = mTodoItem.getTime();
                if (time > 0) {
                    // remind at time
                    String timeText = SimpleDateFormat.getDateTimeInstance().format(time);
                    imageViewItemTypeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);
                    textViewInfo.setText(timeText);
                } else {
                    // remind at location
                    textViewInfo.setText(mTodoItem.getWaypoint().getTitle());
                    imageViewItemTypeIcon.setImageResource(R.drawable.ic_location_on_black_24dp);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    // test edit
    private boolean mIsEditingMode = false;

    @OnClick(R.id.todo_item_edit_button)
    public void onClick(View v) {
        if (mIsEditingMode) {
            mIsEditingMode = false;
            ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(false);
            //hide keyboard
            mInputMethodManager.hideSoftInputFromWindow(mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text).getWindowToken(), 0);
        } else {
            mIsEditingMode = true;
            ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(true);
            mTodoItemTask.scrollToPosition(0);
            EditText et = (EditText) mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text);
            et.setTextIsSelectable(false);
            et.setMovementMethod(ArrowKeyMovementMethod.getInstance());
            et.setCursorVisible(true);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.setSelection(et.getText().length());
            et.requestFocus();
            //show keyboard
            mInputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @OnClick(R.id.todo_item_as_checkboxes_button)
    public void onClickAsCheckboxes(View v) {
        if (mTodoItem.isViewAsCheckboxes()) {
            mTodoItem.setViewAsCheckboxes(false);
            ((Button) v).setText("Show checkboxes");
        } else {
            mTodoItem.setViewAsCheckboxes(true);
            ((Button) v).setText("Hide checkboxes");
        }
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setViewAsCheckboxes(mTodoItem.isViewAsCheckboxes());
    }
}
