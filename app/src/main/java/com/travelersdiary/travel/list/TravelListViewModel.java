package com.travelersdiary.travel.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.travelersdiary.BaseViewModel;
import com.travelersdiary.Constants;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.BaseActivity;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.TravelersDiary;

import java.io.InputStream;

import timber.log.Timber;

public class TravelListViewModel extends BaseViewModel {

    private SharedPreferences sharedPreferences;
    private TravelListAdapter adapter;
    private String userUID;

    @Override
    public void start(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        loadDemoData(context);
    }

    public TravelListAdapter getAdapter() {
        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));
        Query query = firebaseRef.orderByChild(Constants.FIREBASE_TRAVEL_CREATION_TIME).startAt(0);

        adapter = new TravelListAdapter(query);

        return adapter;
    }

    public SharedPreferences getPreferences() {
        return sharedPreferences;
    }

    /**
     * Check first start and store demo data
     */
    public void loadDemoData(final Context context) {
        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Firebase userDataRef = dataSnapshot
                                .getRef() // root/travels/default
                                .getParent() // root/travels
                                .getParent(); // root
                        Travel travel = dataSnapshot.getValue(Travel.class);
                        if (travel == null) {
                            // first start
                            try {
                                InputStream is = context.getAssets().open("demodata.json");
                                int size = is.available();
                                byte[] buffer = new byte[size];
                                is.read(buffer);
                                is.close();
                                String bufferString = new String(buffer);
                                Gson gson = new Gson();
                                TravelersDiary travelersDiary = gson.fromJson(bufferString, TravelersDiary.class);

                                travelersDiary.setName(sharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null));
                                travelersDiary.setEmail(sharedPreferences.getString(Constants.KEY_EMAIL, null));
                                sharedPreferences.edit()
                                        .putString(Constants.KEY_ACTIVE_TRAVEL_KEY,
                                                travelersDiary.getActiveTravel().getActiveTravelKey())
                                        .apply();

                                if (travelersDiary.getActiveTravel() != null &&
                                        !travelersDiary.getActiveTravel()
                                                .getActiveTravelKey()
                                                .equals(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)) {

                                    // TODO
//                                    ((BaseActivity) getActivity()).enableStartTrackingButton(true);
                                } else {
//                                    ((BaseActivity) getActivity()).enableStartTrackingButton(false);
                                }

                                userDataRef.setValue(travelersDiary);
                            } catch (Exception e) {
                                Timber.e(e, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    @Override
    public void stop() {
        if (adapter != null) {
            adapter.cleanup();
        }
    }

}