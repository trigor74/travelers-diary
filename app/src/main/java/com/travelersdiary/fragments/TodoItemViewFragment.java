package com.travelersdiary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.TodoItem;
import com.travelersdiary.models.TodoTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TodoItemViewFragment extends Fragment {
    private ActionBar mSupportActionBar;
    private Firebase mItemRef;
    private TodoItem mTodoItem;

    private String mUserUID;
    private String mKey;

    @Bind(R.id.todo_item_remind_text_container)
    LinearLayout remindTextContainer;
    @Bind(R.id.todo_item_remind_info_text_view)
    TextView textViewInfo;
    @Bind(R.id.todo_item_type_icon)
    ImageView imageViewItemTypeIcon;

    private Context mContext;

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

                final Firebase itemRef = dataSnapshot.getRef();

                ArrayList<TodoTask> todoTaskList = mTodoItem.getTask();
                if (mTodoItem.isViewAsCheckboxes()) {
                    // view as checkboxes
                    for (int i = 0; i < todoTaskList.size(); i++) {
                        TodoTask taskLine = todoTaskList.get(i);

                        AppCompatCheckBox checkBox = new AppCompatCheckBox(mContext);
                        checkBox.setLayoutParams(
                                new LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                        LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                                )
                        );
                        checkBox.setText(taskLine.getItem());
                        checkBox.setChecked(taskLine.isChecked());

                        final Firebase editTaskLineRef = itemRef
                                .child(Constants.FIREBASE_REMINDER_TASK)
                                .child(Integer.toString(i));

                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                // edit
                                Map<String, Object> map = new HashMap<String, Object>();
                                map.put(Constants.FIREBASE_REMINDER_TASK_ITEM_CHECKED, Boolean.toString(isChecked));
                                editTaskLineRef.updateChildren(map);
                            }
                        });

                        remindTextContainer.addView(checkBox);
                    }
                } else {
                    // view as simple lines
                    StringBuilder builder = new StringBuilder();
                    for (TodoTask lines : todoTaskList) {
                        builder.append(lines.getItem() + "\n");
                    }
                    TextView textView = new TextView(mContext);
                    textView.setLayoutParams(
                            new LinearLayoutCompat.LayoutParams(
                                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                            )
                    );
                    textView.setText(builder.toString());
                    remindTextContainer.addView(textView);
                }

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
}
