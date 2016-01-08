package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

public class TravelsListAdapter extends RecyclerView.Adapter<TravelsListAdapter.ViewHolder> {

    /*example of recycler view adapter*/

    private String[] mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public TravelsListAdapter(String[] myDataset) {
        mDataset = myDataset;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
//        @Bind(R.id.text_view)
//        TextView textView;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TravelsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        // set the view's size, margins, paddings and layout parameters

//        return new ViewHolder(view);
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        holder.textView.setText(mDataset[position]);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
