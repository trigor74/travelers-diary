package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.travelersdiary.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesListAdapter extends RecyclerView.Adapter<DiaryImagesListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mImagesList;

    public DiaryImagesListAdapter(Context context, ArrayList<String> list) {
        this.mContext = context;
        this.mImagesList = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.img_diary_note)
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void changeList(ArrayList<String> list) {
        this.mImagesList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_diary_note_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso.with(mContext)
                .load(mImagesList.get(position))
                .resize(150, 150)
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return mImagesList.size();
    }

}
