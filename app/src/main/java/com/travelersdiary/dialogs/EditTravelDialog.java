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

import com.firebase.client.Firebase;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
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
            mTravelKey = arguments.getString(Constants.KEY_TRAVEL_KEY);
            if (mTravelKey != null && !mTravelKey.isEmpty()){
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
                        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                        Firebase firebaseRef = new Firebase(Constants.FIREBASE_URL)
                                .child("users")
                                .child(userUID)
                                .child("travels");

                        if (mTravelKey == null || mTravelKey.isEmpty()) {
                            Travel travel = new Travel();
                            travel.setTitle(title);
                            travel.setDescription(description);
                            travel.setStart(currentTime);
                            travel.setStop(-1);
                            travel.setActive(false);

                            Firebase newTravelRef = firebaseRef.push();
                            newTravelRef.setValue(travel);
                        } else {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put(Constants.FIREBASE_TRAVEL_TITLE, title);
                            map.put(Constants.FIREBASE_TRAVEL_DESCRIPTION, description);
                            Firebase editTravelRef = firebaseRef.child(mTravelKey);
                            editTravelRef.updateChildren(map);
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
