package com.travelersdiary.travel.list;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.databinding.ListItemTravelBinding;
import com.travelersdiary.models.Travel;

public class TravelListAdapter extends FirebaseRecyclerAdapter<Travel, TravelListAdapter.ViewHolder> {

    public TravelListAdapter(Firebase ref) {
        super(Travel.class, R.layout.list_item_travel, TravelListAdapter.ViewHolder.class, ref);
    }

    public TravelListAdapter(Query ref) {
        super(Travel.class, R.layout.list_item_travel, TravelListAdapter.ViewHolder.class, ref);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ListItemTravelBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
//            v.setLongClickable(true);
        }

        void bindTravel(Travel travel, String key) {
            binding.setViewModel(new TravelListItemViewModel(travel, key));
        }
    }

    @Override
    protected void populateViewHolder(TravelListAdapter.ViewHolder holder, Travel model, int position) {
        holder.bindTravel(model, this.getRef(position).getKey());
    }

}
