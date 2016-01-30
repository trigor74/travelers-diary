package com.travelersdiary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.travelersdiary.models.TodoItem;

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

    private TodoTaskAdapter mAdapter;

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

                Firebase itemRef = dataSnapshot.getRef();
                Query todoTaskList = itemRef.child(Constants.FIREBASE_REMINDER_TASK).orderByKey();

                mAdapter = new TodoTaskAdapter(todoTaskList, mTodoItem.isViewAsCheckboxes());
//                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mTodoItemTask.setLayoutManager(layoutManager);
                mTodoItemTask.setItemAnimator(new DefaultItemAnimator());
                mTodoItemTask.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();

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
//            mRtEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//            mRtEditText.setFocusable(false);
//            android:focusableInTouchMode="false"
        } else {
            mIsEditingMode = true;
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
        mAdapter.setViewAsCheckboxes(mTodoItem.isViewAsCheckboxes());
    }
}
