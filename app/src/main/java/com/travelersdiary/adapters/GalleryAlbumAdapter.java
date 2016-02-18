package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.travelersdiary.R;
import com.travelersdiary.models.AlbumsModel;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryAlbumAdapter extends RecyclerView.Adapter<GalleryAlbumAdapter.ViewHolder> {

    private ArrayList<AlbumsModel> mGalleryImagesList;
    private Context mContext;
    private static OnItemClickListener mItemClickListener;

    public GalleryAlbumAdapter(Context context, ArrayList<AlbumsModel> galleryImagesList) {
        this.mGalleryImagesList = galleryImagesList;
        this.mContext = context;
    }

    @Override
    public GalleryAlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_album_item, null);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.albumName.setText(mGalleryImagesList.get(position).getFolderName());
        viewHolder.albumCount.setText(String.valueOf(mGalleryImagesList.get(position).folderImages.size()));

        Glide.with(mContext)
                .load("file://" + mGalleryImagesList.get(position).getFolderImagePath())
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .crossFade()
                .into(viewHolder.imgAlbum);
    }

    @Override
    public int getItemCount() {
        return mGalleryImagesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.txt_album_name)
        TextView albumName;
        @Bind(R.id.txt_album_count)
        TextView albumCount;
        @Bind(R.id.img_gallery_album)
        ImageView imgAlbum;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        GalleryAlbumAdapter.mItemClickListener = mItemClickListener;
    }

    public ArrayList<AlbumsModel> getGalleryImagesList() {
        return mGalleryImagesList;
    }

}
