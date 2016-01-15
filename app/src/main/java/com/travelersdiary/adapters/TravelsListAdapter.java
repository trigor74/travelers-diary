package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.models.Travel;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListAdapter extends FirebaseRecyclerAdapter<Travel, TravelsListAdapter.ViewHolder> {

    public TravelsListAdapter(Firebase ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
    }

    public TravelsListAdapter(Query ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_travel_title_text_view)
        TextView textViewTitle;
        @Bind(R.id.item_travel_description_text_view)
        TextView textViewDescription;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    protected void populateViewHolder(TravelsListAdapter.ViewHolder viewHolder, Travel model, int position) {
        viewHolder.textViewTitle.setText(model.getTitle());
        viewHolder.textViewDescription.setText(model.getDescription());
    }
}
