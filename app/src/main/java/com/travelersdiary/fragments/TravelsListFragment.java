package com.travelersdiary.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.TravelActivity;
import com.travelersdiary.adapters.TravelsListAdapter;
import com.travelersdiary.dialogs.EditTravelDialog;
import com.travelersdiary.models.Travel;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListFragment extends Fragment {

    @Bind(R.id.travels_list)
    RecyclerView mTravelsList;

    private FirebaseRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_travels_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mTravelsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayoutManager = new LinearLayoutManager(getContext());
        mTravelsList.setLayoutManager(mLayoutManager);

        // animation
        mTravelsList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mTravelsList.addItemDecoration(itemDecoration);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Travel travel = dataSnapshot.getValue(Travel.class);
                        if (travel == null) {
                            // first start
                            travel = new Travel();
                            travel.setTitle(getString(R.string.default_travel_title));
                            travel.setDescription(getString(R.string.default_travel_description));
                            travel.setStart(-1);
                            travel.setStop(-1);
                            travel.setActive(false);

                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                                    .setValue(travel);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("firebase", "firebase onCancelled: " + firebaseError.getMessage());
                    }
                });

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID));
        Query query;

        query = mFirebaseRef.orderByKey();

        mAdapter = new TravelsListAdapter(query);
        mTravelsList.setAdapter(mAdapter);

        ((TravelsListAdapter) mAdapter).setOnItemClickListener(new TravelsListAdapter.OnItemClickListener () {
            @Override
            public void onItemClick(View view, int position) {
                String key = mAdapter.getRef(position).getKey();

                // get travel data
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                Firebase itemRef = new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                        .child(key);

                itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Travel travel = dataSnapshot.getValue(Travel.class);

                        Intent intent = new Intent(getActivity(), TravelActivity.class);
                        intent.putExtra(Constants.KEY_TRAVEL_REF, dataSnapshot.getKey());
                        intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, int position) {
/*
                Firebase itemRef = mAdapter.getRef(position);
                Travel travel = ((TravelsListAdapter) mAdapter).getItem(position);
//                Toast.makeText(getContext(), travel.getTitle() + " was deleted!", Toast.LENGTH_SHORT).show();
//                itemRef.removeValue();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                EditTravelDialog addTravelDialog = new EditTravelDialog();

                Bundle bundle = new Bundle();
                bundle.putString(Constants.KEY_TRAVEL_REF, itemRef.getKey());
                bundle.putString(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                bundle.putString(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
                addTravelDialog.setArguments(bundle);

                addTravelDialog.show(fragmentManager, "edit_travel_dialog");
*/
            }
        });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Log.d("CTX", "info.id " + info.id);
        } else {
            Log.d("CTX", "info.id = null");
        }
        Log.d("CTX", "item.getItemId() " + item.getItemId());
        return super.onContextItemSelected(item);

/*
        switch (item.getItemId()) {
            case R.id.edit:
                return true;
            case R.id.delete:
                deleteNote(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }

}
