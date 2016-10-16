package com.travelersdiary.travel.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.travelersdiary.BaseViewModel;
import com.travelersdiary.Constants;
import com.travelersdiary.Utils;

public class TravelListViewModel extends BaseViewModel {

    private SharedPreferences sharedPreferences;

    @Override
    public void start(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public TravelListAdapter getAdapter() {
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase firebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));
        Query query = firebaseRef.orderByChild(Constants.FIREBASE_TRAVEL_CREATION_TIME).startAt(0);

        final TravelListAdapter adapter = new TravelListAdapter(query);

//        adapter.setOnItemClickListener(new TravelListAdapter.OnItemClickListener () {
//            @Override
//            public void onItemClick(View view, int position) {
//                String key = adapter.getRef(position).getKey();
//                Travel travel = adapter.getItem(position);
//
//                Intent intent = new Intent(context, TravelActivity.class);
//                intent.putExtra(Constants.KEY_TRAVEL_REF, key);
//                intent.putExtra("Travel", travel);
//
////                intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
////                intent.putExtra(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
////                intent.putExtra(Constants.KEY_TRAVEL_DEFAULT_COVER, travel.getDefaultCover());
////                intent.putExtra(Constants.KEY_TRAVEL_USER_COVER, travel.getUserCover());
////                intent.putExtra(Constants.KEY_TRAVEL_IS_ACTIVE, travel.isActive());
////                intent.putExtra(Constants.KEY_TRAVEL_CREATION_TIME, travel.getCreationTime());
////                intent.putExtra(Constants.KEY_TRAVEL_START_TIME, travel.getStart());
////                intent.putExtra(Constants.KEY_TRAVEL_STOP_TIME, travel.getStop());
//                context.startActivity(intent);
//            }
//        });

        return adapter;
    }

    @Override
    public void stop() {
        this.context = null;
    }

    @BindingAdapter({"adapter"})
    public static void bindAdapter(RecyclerView list, RecyclerView.Adapter adapter) {
        list.setAdapter(adapter);
    }
}
