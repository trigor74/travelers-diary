package com.travelersdiary.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.FullScreenImageActivity;
import com.travelersdiary.models.Photo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesListAdapter extends RecyclerView.Adapter<DiaryImagesListAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Photo> mImagesList;

    public DiaryImagesListAdapter(Context context, ArrayList<Photo> list) {
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

    public void changeList(ArrayList<Photo> list) {
        this.mImagesList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_diary_note_image, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FullScreenImageActivity.class);
                intent.putExtra("images",  mImagesList);
                intent.putExtra("position", viewHolder.getAdapterPosition());
                mContext.startActivity(intent);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (Utils.checkFileExists(mContext, mImagesList.get(position).getLocalUri())) {
            Glide.with(mContext)
                    .load(mImagesList.get(position).getLocalUri())
                    .centerCrop()
                    .into(holder.image);
        } else {
            Glide.with(mContext)
                    .load(mImagesList.get(position).getPicasaUri())
                    .centerCrop()
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return mImagesList.size();
    }

}
