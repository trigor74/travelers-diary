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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
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
import com.travelersdiary.models.TodoTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RemindItemFragment extends Fragment {
    private static String DATE_PICKER_DIALOG_TAG = "DatePickerDialog";
    private static String TIME_PICKER_DIALOG_TAG = "TimePickerDialog";

    private ActionBar mSupportActionBar;
    private Menu mMenu;
    private Firebase mItemRef;
    private TodoItem mRemindItem;

    private String mUserUID;
    private String mItemKey;

    private boolean isEditingMode = false;
    private boolean hasChanged = false;

    @Bind(R.id.remind_item_dont_remind_text_view)
    TextView dontRemindTextView;
    @Bind(R.id.remind_item_remind_type_spinner)
    Spinner remindTypeSpinner;
    @Bind(R.id.remind_item_date_text_view)
    TextView dateTextView;
    @Bind(R.id.remind_item_time_text_view)
    TextView timeTextView;
    @Bind(R.id.remind_item_waypoint_title_text_view)
    TextView waypointTitle;
    @Bind(R.id.remind_item_waypoint_distance_text_view)
    TextView waypointDistance;
    @Bind(R.id.remind_item_task)
    RecyclerView mTodoItemTask;

    private Context mContext;

    private EditText mRemindItemTitleEditText;
    private RecyclerView.Adapter mAdapter;

    private InputMethodManager mInputMethodManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remind_item, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        mItemKey = getArguments().getString(Constants.KEY_TODO_ITEM_REF);

        //get toolbar
        mSupportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            mSupportActionBar.setDisplayShowTitleEnabled(false);
        }

        mRemindItemTitleEditText = (EditText) (getActivity()).findViewById(R.id.remind_item_title_edit_text);
        mRemindItemTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.colorAccent);
                } else {
                    Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.white);
                    if (mRemindItemTitleEditText.getText().toString().trim().length() == 0) {
                        mRemindItemTitleEditText.setText(mRemindItem.getTitle());
                    }
                }
            }
        });

        mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mItemKey != null && !mItemKey.isEmpty()) {
            retrieveData(mItemKey);
        } else {
            // create new empty item
            mRemindItem = new TodoItem();
            mRemindItem.setTitle(mContext.getString(R.string.reminder_new_remind_item_default_title));
            mRemindItem.setViewAsCheckboxes(false);
            ArrayList<TodoTask> task = new ArrayList<>();
            task.add(new TodoTask("", false));
            mRemindItem.setTask(task);
        }

        DatePickerDialog datePickerDialog = (DatePickerDialog) getActivity().getFragmentManager().findFragmentByTag(DATE_PICKER_DIALOG_TAG);
        TimePickerDialog timePickerDialog = (TimePickerDialog) getActivity().getFragmentManager().findFragmentByTag(TIME_PICKER_DIALOG_TAG);
        if (datePickerDialog != null) datePickerDialog.setOnDateSetListener(mDateSetListener);
        if (timePickerDialog != null) timePickerDialog.setOnTimeSetListener(mTimeSetListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.remind_item_editor_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isEditingMode) {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            menu.setGroupVisible(R.id.remind_item_menu_edit_mode, true);
            menu.setGroupVisible(R.id.remind_item_menu_view_mode, false);
        } else {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            menu.setGroupVisible(R.id.remind_item_menu_edit_mode, false);
            menu.setGroupVisible(R.id.remind_item_menu_view_mode, true);
        }

        if (mRemindItem != null && mRemindItem.isViewAsCheckboxes()) {
            menu.findItem(R.id.action_switch_checkboxes_remind_item)
                    .setTitle(R.string.remind_item_hide_checkboxes);
        } else {
            menu.findItem(R.id.action_switch_checkboxes_remind_item)
                    .setTitle(R.string.remind_item_show_checkboxes);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isEditingMode) {
                    if (hasChanged) {
                        // TODO: 25.02.16 show discard dialog
//                        showDiscardDialog();
                    } else {
                        enableReviewingMode();
                    }
                } else {
                    getActivity().finish();
                }
                return true;
            case R.id.action_save_remind_item:
                saveItem();
                return true;
            case R.id.action_edit_remind_item:
                enableEditingMode();
                return true;
            case R.id.action_switch_checkboxes_remind_item:
                if (mRemindItem.isViewAsCheckboxes()) {
                    mRemindItem.setViewAsCheckboxes(false);
                    item.setTitle(R.string.remind_item_show_checkboxes);
                } else {
                    mRemindItem.setViewAsCheckboxes(true);
                    item.setTitle(R.string.remind_item_hide_checkboxes);
                }
                ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setViewAsCheckboxes(mRemindItem.isViewAsCheckboxes());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                mRemindItem = dataSnapshot.getValue(TodoItem.class);
                mRemindItemTitleEditText.setText(mRemindItem.getTitle());
                if (mMenu != null && mRemindItem.isViewAsCheckboxes()) {
                    mMenu.findItem(R.id.action_switch_checkboxes_remind_item)
                            .setTitle(R.string.remind_item_hide_checkboxes);
                    //refresh toolbar
                    mSupportActionBar.invalidateOptionsMenu();
                }

                // item type
                RemindTypesAdapter adapter = new RemindTypesAdapter(mContext);
                remindTypeSpinner.setAdapter(adapter);

                // task text
                mAdapter = new TodoTaskAdapter(mRemindItem.getTask(), mRemindItem.isViewAsCheckboxes());
                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                mTodoItemTask.setLayoutManager(layoutManager);
                mTodoItemTask.setAdapter(mAdapter);

                // item remind data
                if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(mRemindItem.getType())) {
                    // remind at time
                    setDateTimeText();
                } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(mRemindItem.getType())) {
                    // remind at location
                    setLocationAndDistanceText();
                } else {
                    // don't remind
                }

                setRemindTypeViewVisibility(mRemindItem.getType());
                remindTypeSpinner.setVisibility(View.VISIBLE);
                setOnClickListeners();
                setEditingMode(isEditingMode);
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
            long time = mRemindItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(year, monthOfYear, dayOfMonth);
            mRemindItem.setTime(c.getTimeInMillis());
            setDateTimeText();
        }
    };

    private void setDateTimeText() {
        long timestamp = mRemindItem.getTime();
        String dateText = SimpleDateFormat.getDateInstance().format(timestamp);
        String timeText = SimpleDateFormat.getTimeInstance().format(timestamp);
        dateTextView.setText(dateText);
        timeTextView.setText(timeText);
    }

    private void setLocationAndDistanceText() {
        waypointTitle.setText(mRemindItem.getWaypoint().getTitle());
        waypointDistance.setText(Integer.toString(mRemindItem.getDistance()));
    }

    private void openDatePicker() {
        Calendar c = Calendar.getInstance();
        long time = mRemindItem.getTime();
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
            long time = mRemindItem.getTime();
            if (time > 0) {
                c.setTimeInMillis(time);
            }
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            mRemindItem.setTime(c.getTimeInMillis());
            setDateTimeText();
        }
    };

    private void openTimePicker() {
        Calendar c = Calendar.getInstance();
        long time = mRemindItem.getTime();
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

        remindTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // don't remind
                        mRemindItem.setType("");
                        break;
                    case 1: // remind at time
                        mRemindItem.setType(Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME);
                        if (mRemindItem.getTime() <= 0) {
                            mRemindItem.setTime(System.currentTimeMillis());
                        }
                        break;
                    case 2: // remind at location
                        mRemindItem.setType(Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION);
                        break;
                }
                setRemindTypeViewVisibility(mRemindItem.getType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
            remindTypeSpinner.setSelection(1);
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.VISIBLE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(type)) {
            // remind at location
            remindTypeSpinner.setSelection(2);
            dontRemindTextView.setVisibility(View.GONE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.VISIBLE);
            waypointDistance.setVisibility(View.VISIBLE);
        } else {
            // don't remind
            remindTypeSpinner.setSelection(0);
            dontRemindTextView.setVisibility(View.VISIBLE);
            dateTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            waypointTitle.setVisibility(View.GONE);
            waypointDistance.setVisibility(View.GONE);
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
        isEditingMode = editingMode;
        if (editingMode) {
            mRemindItemTitleEditText.setFocusable(true);
            mRemindItemTitleEditText.setFocusableInTouchMode(true);
            Utils.tintWidget(getContext(), mRemindItemTitleEditText, R.color.white);

        } else {
            mRemindItemTitleEditText.setFocusable(false);
            Utils.tintWidget(getContext(), mRemindItemTitleEditText, android.R.color.transparent);
        }

        setViewEditMode(dontRemindTextView, editingMode);
        setViewEditMode(dateTextView, editingMode);
        setViewEditMode(timeTextView, editingMode);
        setViewEditMode(waypointTitle, editingMode);
        setViewEditMode(waypointDistance, editingMode);
        setSpinnerEditMode(remindTypeSpinner, editingMode);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setEditable(editingMode);
    }

    private void enableReviewingMode() {
        setEditingMode(false);
        //hide keyboard
        mInputMethodManager.hideSoftInputFromWindow(mTodoItemTask.findFocus().findViewById(R.id.task_item_edit_text).getWindowToken(), 0);
        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void enableEditingMode() {
        setEditingMode(true);
        mTodoItemTask.scrollToPosition(0);
        ((TodoTaskAdapter) mTodoItemTask.getAdapter()).setSelectedItem(0);
        EditText et = (EditText) mTodoItemTask
                .findViewHolderForLayoutPosition(0)
                .itemView
                .findViewById(R.id.task_item_edit_text);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.setSelection(1);
        et.setCursorVisible(true);
        et.setSelected(true);
        et.requestFocus();
        //show keyboard
        mInputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        //refresh toolbar
        mSupportActionBar.invalidateOptionsMenu();
    }

    public void saveItem() {
        mRemindItem.setTitle(mRemindItemTitleEditText.getText().toString());
        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserReminderUrl(mUserUID));
        if (mItemKey != null && !mItemKey.isEmpty()) {
            // update item
            Firebase updateItemRef = firebaseRef.child(mItemKey);
            updateItemRef.setValue(mRemindItem);
        } else {
            // create item
            Firebase newItemRef = firebaseRef.push();
            newItemRef.setValue(mRemindItem);
            mItemKey = newItemRef.getKey();
        }
    }
}
