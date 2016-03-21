package com.travelersdiary.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.Travel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EditTravelDialog extends DialogFragment {

    @Bind(R.id.new_travel_title_edit_text)
    EditText mTravelTitle;
    @Bind(R.id.new_travel_description_edit_text)
    EditText mTravelDescription;

    private String mTravelKey;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_add_travel, null);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        String dialogTitle = getString(R.string.edit_travel_dialog_title_create);
        String positiveButtonText = getString(R.string.create);
        if (arguments != null) {
            mTravelTitle.setText(arguments.getString(Constants.KEY_TRAVEL_TITLE));
            mTravelDescription.setText(arguments.getString(Constants.KEY_TRAVEL_DESCRIPTION));
            mTravelKey = arguments.getString(Constants.KEY_TRAVEL_REF);
            if (mTravelKey != null && !mTravelKey.isEmpty()) {
                dialogTitle = getString(R.string.edit_travel_dialog_title_edit);
                positiveButtonText = getString(R.string.save);
            }
        }

        builder.setView(view)
                .setTitle(dialogTitle)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        long currentTime = System.currentTimeMillis();

                        String title = mTravelTitle.getText().toString();
                        String description = mTravelDescription.getText().toString();
                        // Set default title if none
                        if (title == null || title.isEmpty()) {
                            title = getString(R.string.edit_travel_dialog_default_title, SimpleDateFormat.getDateTimeInstance().format(currentTime));
                        }

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        final String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));

                        if (mTravelKey == null || mTravelKey.isEmpty()) {
                            // create
                            Travel travel = new Travel();
                            travel.setTitle(title);
                            travel.setDescription(description);
                            travel.setStart(currentTime);
                            travel.setStop(-1);
                            travel.setActive(false);

                            Firebase newTravelRef = firebaseRef.push();
                            newTravelRef.setValue(travel);
                        } else {
                            // edit
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put(Constants.FIREBASE_TRAVEL_TITLE, title);
                            map.put(Constants.FIREBASE_TRAVEL_DESCRIPTION, description);
                            Firebase editTravelRef = firebaseRef.child(mTravelKey);
                            editTravelRef.updateChildren(map);

                            // update all notes with new travel title
                            final String newTravelTitle = title;

                            Firebase diaryRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
                            Query queryRef = diaryRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(mTravelKey);

                            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put(Constants.FIREBASE_DIARY_TRAVEL_TITLE, newTravelTitle);
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        child.getRef().updateChildren(map);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });

                            // update all reminder items with new travel title

                            Firebase reminderRef = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));
                            Query reminderQueryRef = reminderRef.orderByChild(Constants.FIREBASE_REMINDER_TRAVELID).equalTo(mTravelKey);

                            reminderQueryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put(Constants.FIREBASE_REMINDER_TRAVEL_TITLE, newTravelTitle);
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        child.getRef().updateChildren(map);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditTravelDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ButterKnife.unbind(this);
        super.onDismiss(dialog);
    }
}
