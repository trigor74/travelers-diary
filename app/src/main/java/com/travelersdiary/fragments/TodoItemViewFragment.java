package com.travelersdiary.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.RemindTypesAdapter;
import com.travelersdiary.adapters.TodoTaskAdapter;
import com.travelersdiary.models.TodoItem;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TodoItemViewFragment extends Fragment {
    private static String DATE_PICKER_DIALOG_TAG = "DatePickerDialog";
    private static String TIME_PICKER_DIALOG_TAG = "TimePickerDialog";

    private ActionBar mSupportActionBar;
    private Firebase mItemRef;
    private TodoItem mTodoItem;

    private String mUserUID;
    private String mKey;

    private boolean mIsEditingMode = false;

    @Bind(R.id.todo_item_dont_remind_text_view)
    TextView dontRemindTextView;
    @Bind(R.id.todo_item_remind_type_spinner)
    Spinner remindTypeSpinner;
    @Bind(R.id.todo_item_date_text_view)
    TextView dateTextView;
    @Bind(R.id.todo_item_time_text_view)
    TextView timeTextView;
    @Bind(R.id.todo_item_waypoint_title_text_view)
    TextView waypointTitle;
    @Bind(R.id.todo_item_waypoint_distance_text_view)
    TextView waypointDistance;

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        retrieveData(mKey);

        DatePickerDialog datePickerDialog = (DatePickerDialog) getActivity().getFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG_TAG);
        TimePickerDialog timePickerDialog = (TimePickerDialog) getActivity().getFragmentManager().findFragmentByTag(TIME_PICKER_DIALOG_TAG);
        if (datePickerDialog != null) datePickerDialog.setOnDateSetListener(mDateSetListener);
        if (timePickerDialog != null) timePickerDialog.setOnTimeSetListener(mTimeSetListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void retrieveData(String key) {
        mItemRef = new Firebase(Utils.getFirebaseUserReminderUrl(mUserUID))
                .child(key);
        mItemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTodoItem = dataSnapshot.getValue(TodoItem.class);
                mSupportActionBar.setTitle(mTodoItem.getTitle());

                // item type
                RemindTypesAdapter adapter = new RemindTypesAdapter(mContext);
                remindTypeSpinner.setAdapter(adapter);

                // task text
                mAdapter = new TodoTaskAdapter(mTodoItem.getTask(), mTodoItem.isViewAsCheckboxes());
                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mTodoItemTask.setLayoutManager(layoutManager);
                mTodoItemTask.setAdapter(mAdapter);

                // item remind data
                if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(mTodoItem.getType())) {
                    // remind at time
                    long time = mTodoItem.getTime();
                    String timeText = SimpleDateFormat.getDateTimeInstance().format(time);
                    dateTextView.setText(timeText);
                    timeTextView.setText("00:00:00");
                } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(mTodoItem.getType())) {
                    // remind at location
                    waypointTitle.setText(mTodoItem.getWaypoint().getTitle());
                    waypointDistance.setText("100");
                } else {
                    // don't remind
                }

                setRemindTypeViewVisibility(mTodoItem.getType());
                remindTypeSpinner.setVisibility(View.VISIBLE);
                setOnClickListeners();
                setEditingMode(mIsEditingMode);
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

    DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance();
            long time = mTodoItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(year, monthOfYear, dayOfMonth);
            mTodoItem.setTime(c.getTimeInMillis());
            // TODO: 10.02.16 change date format!!!
            dateTextView.setText(SimpleDateFormat.getDateTimeInstance().format(mTodoItem.getTime()));
        }
    };

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        long time = mTodoItem.getTime();
        if (time > 0) {
            c.setTimeInMillis(time);
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(mDateSetListener, year, month, day);
//        datePickerDialog.dismissOnPause(true);
        datePickerDialog.vibrate(false);
        datePickerDialog.show(getActivity().getFragmentManager(), DATE_PICKER_DIALOG_TAG);
    }

    TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
            Calendar c = Calendar.getInstance();
            long time = mTodoItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            mTodoItem.setTime(c.getTimeInMillis());
            // TODO: 10.02.16 change time format!!!
            timeTextView.setText(SimpleDateFormat.getDateTimeInstance().format(mTodoItem.getTime()));
        }
    };

    private void openTimePicker() {
        Calendar c = Calendar.getInstance();
        long time = mTodoItem.getTime();
        if (time > 0) {
            c.setTimeInMillis(time);
        }
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(mTimeSetListener, hour, minute, true);
//        timePickerDialog.dismissOnPause(true);
        timePickerDialog.vibrate(false);
        timePickerDialog.show(getActivity().getFragmentManager(), TIME_PICKER_DIALOG_TAG);
    }

    private void setOnClickListeners() {
        dontRemindTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dontRemindTextView.clearFocus();
                remindTypeSpinner.requestFocus();
                remindTypeSpinner.performClick();
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimePicker();
            }
        });

        waypointTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "TEST WAYPOINT", Toast.LENGTH_SHORT).show();
            }
        });

        waypointDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "TEST DISTANCE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRemindTypeViewVisibility(String type) {
        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(type)) {
            // remind at time
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.VISIBLE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
            remindTypeSpinner.setSelection(1);
        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
            // remind at location
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.VISIBLE);
            waypointDistance.setVisibility(View.VISIBLE);
            remindTypeSpinner.setSelection(2);
        } else {
            // don't remind
            dontRemindTextView.setVisibility(View.VISIBLE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
            remindTypeSpinner.setSelection(0);
        }
    }

    private void setViewEditMode(View v, boolean editMode) {
        if (editMode) {
            v.setClickable(true);
            v.setLongClickable(true);
//            v.setFocusable(true);
//            v.setFocusableInTouchMode(true);
            v.setBackground(ContextCompat.getDrawable(mContext, R.drawable.abc_edit_text_material));
        } else {
            v.setClickable(false);
            v.setLongClickable(false);
//            v.setFocusable(false);
//            v.setFocusableInTouchMode(false);
            v.setBackground(null);
        }
    }

    private void setSpinnerEditMode(Spinner spinner, boolean editMode) {
        if (editMode) {
            spinner.setEnabled(true);
            spinner.setBackground(ContextCompat.getDrawable(mContext, R.drawable.abc_edit_text_material));
        } else {
            spinner.setEnabled(false);
            spinner.setBackground(null);
        }
    }

    private void setEditingMode(boolean editingMode) {
        setViewEditMode(dontRemindTextView, editingMode);
        setViewEditMode(dateTextView, editingMode);
        setViewEditMode(timeTextView, editingMode);
        setViewEditMode(waypointTitle, editingMode);
        setViewEditMode(waypointDistance, editingMode);
        setSpinnerEditMode(remindTypeSpinner, editingMode);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(editingMode);
    }

    @OnClick(R.id.todo_item_edit_button)
    public void onClickEdit(View v) {
        if (mIsEditingMode) {
            mIsEditingMode = false;
            setEditingMode(false);
            //hide keyboard
            mInputMethodManager.hideSoftInputFromWindow(mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text).getWindowToken(), 0);
        } else {
            mIsEditingMode = true;
            setEditingMode(true);

            mTodoItemTask.scrollToPosition(0);
            ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setSelectedItem(0);
//            EditText et = (EditText) mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text);
            EditText et = (EditText) mTodoItemTask
                    .findViewHolderForLayoutPosition(0)
                    .itemView
                    .findViewById(R.id.task_item_edit_text);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
//            et.setSelection(et.getText().length());
            et.setSelection(1);
            et.setCursorVisible(true);
            et.setSelected(true);
            et.requestFocus();
            //show keyboard
            mInputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @OnClick(R.id.todo_item_as_checkboxes_button)
    public void onClickViewAsCheckboxes(View v) {
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
