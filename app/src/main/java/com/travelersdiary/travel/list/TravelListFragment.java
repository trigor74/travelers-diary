package com.travelersdiary.travel.list;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
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

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.EditTravelActivity;
import com.travelersdiary.databinding.FragmentTravelListBinding;
import com.travelersdiary.models.Travel;
import com.travelersdiary.recyclerview.FirebaseContextMenuRecyclerView;
import com.travelersdiary.travel.TravelActivity;

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

        setupList(binding.travelsList);

        viewModel.start(getContext());
    }

    private void setupList(RecyclerView list) {
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setItemAnimator(new DefaultItemAnimator());

        registerForContextMenu(list);
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
            Travel travel = viewModel.getAdapter().getItem(info.position);

            String activeTravel = viewModel.getPreferences().getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            menu.findItem(R.id.menu_item_start).setVisible(!info.ref.getKey().equals(activeTravel));
            menu.findItem(R.id.menu_item_stop).setVisible(travel.getStart() > 0 && travel.getStop() < 0);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo info =
                (FirebaseContextMenuRecyclerView.FirebaseRecyclerViewContextMenuInfo) item.getMenuInfo();
        if (info != null) {
            Travel travel = viewModel.getAdapter().getItem(info.position);

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
    public void onDestroy() {
        viewModel.stop();
        super.onDestroy();
    }

}
