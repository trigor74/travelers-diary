package com.travelersdiary.travel.list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.BaseActivity;
import com.travelersdiary.activities.EditTravelActivity;
import com.travelersdiary.databinding.FragmentTravelListBinding;
import com.travelersdiary.travel.TravelActivity;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.TravelersDiary;
import com.travelersdiary.recyclerview.FirebaseContextMenuRecyclerView;

import java.io.InputStream;

import butterknife.ButterKnife;

public class TravelListFragment extends Fragment {

    private FragmentTravelListBinding binding;
    private TravelListViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_travel_list, container, false);

        viewModel = new TravelListViewModel();
        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // check first start and store demo data
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
                            /*
                            // minimum - default travel
                            travel = new Travel();
                            travel.setTitle(getString(R.string.default_travel_title));
                            travel.setDescription(getString(R.string.default_travel_description));
                            travel.setCreationTime(-1);
                            travel.setStart(-1);
                            travel.setStop(-1);
                            travel.setActive(false);

                            Strong cover = ContentResolver.SCHEME_ANDROID_RESOURCE +
                                    "://" + getResources().getResourcePackageName(R.drawable.travel_cover_default)
                                    + '/' + getResources().getResourceTypeName(R.drawable.travel_cover_default)
                                    + '/' + getResources().getResourceEntryName(R.drawable.travel_cover_default);
                            travel.setCover(cover);

                            new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                    .child(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)
                                    .setValue(travel);
                            */

                            try {
                                InputStream is = getActivity().getAssets().open("demodata.json");
                                int size = is.available();
                                byte[] buffer = new byte[size];
                                is.read(buffer);
                                is.close();
                                String bufferString = new String(buffer);
                                Gson gson = new Gson();
                                TravelersDiary travelersDiary = gson.fromJson(bufferString, TravelersDiary.class);

                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                travelersDiary.setName(sharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null));
                                travelersDiary.setEmail(sharedPreferences.getString(Constants.KEY_EMAIL, null));
                                sharedPreferences.edit().putString(Constants.KEY_ACTIVE_TRAVEL_KEY, travelersDiary.getActiveTravel().getActiveTravelKey()).apply();
                                if (travelersDiary.getActiveTravel() != null &&
                                        !travelersDiary.getActiveTravel().getActiveTravelKey().equals(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)) {
                                    ((BaseActivity) getActivity()).enableStartTrackingButton(true);
                                } else {
                                    ((BaseActivity) getActivity()).enableStartTrackingButton(false);
                                }

                                userDataRef.setValue(travelersDiary);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });


        registerForContextMenu(mTravelList);
    }

    private void setupList(RecyclerView list) {
        mTravelList.setLayoutManager(new LinearLayoutManager(getContext()));
        mTravelList.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.travel_list_item_context, menu);
        menu.setHeaderTitle(R.string.travels_select_action_text);

        FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) menuInfo;
        if (info != null) {
            Travel travel = ((TravelListAdapter) mAdapter).getItem(info.position);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String activeTravel = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            menu.findItem(R.id.menu_item_start).setVisible(!info.ref.getKey().equals(activeTravel));
            menu.findItem(R.id.menu_item_stop).setVisible(travel.getStart() > 0 && travel.getStop() < 0);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Travel travel = ((TravelListAdapter) mAdapter).getItem(info.position);

            switch (item.getItemId()) {
                case R.id.menu_item_start:
                    Utils.startTravel(getContext(), info.ref.getKey(), travel.getTitle());
                    return true;
                case R.id.menu_item_stop:
                    Utils.stopTravel(getContext(), info.ref.getKey());
                    return true;
                case R.id.menu_item_open:
                    Intent intent = new Intent(getActivity(), TravelActivity.class);
                    intent.putExtra(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    startActivity(intent);
                    return true;
                case R.id.menu_item_edit:
                    Intent editIntent = new Intent(getActivity(), EditTravelActivity.class);
                    editIntent.putExtra(Constants.KEY_TRAVEL_REF, info.ref.getKey());
                    editIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                    editIntent.putExtra(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
                    editIntent.putExtra(Constants.KEY_TRAVEL_DEFAULT_COVER, travel.getDefaultCover());
                    editIntent.putExtra(Constants.KEY_TRAVEL_USER_COVER, travel.getUserCover());
                    startActivity(editIntent);
                    return true;
                case R.id.menu_item_delete:
                    if (Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(info.ref.getKey())) {
                        return true;
                    }
                    Utils.deleteTravel(getContext(), info.ref.getKey());
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
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
