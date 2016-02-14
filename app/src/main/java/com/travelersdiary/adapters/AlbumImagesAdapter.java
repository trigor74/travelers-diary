package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.travelersdiary.R;
import com.travelersdiary.activities.AlbumImagesActivity;
import com.travelersdiary.models.AlbumsModel;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlbumImagesAdapter extends SelectableAdapter<AlbumImagesAdapter.ViewHolder> {

    public Context mContext;
    private ArrayList<String> mAlbumImages;
    public boolean mShowCheckBox;
    private ViewHolder.ClickListener clickListener;

    static RelativeLayout.LayoutParams params;
    int width;
    int height;

    public AlbumImagesAdapter(Context context, ArrayList<String> galleryImagesList, ViewHolder.ClickListener clickListener) {
        this.mAlbumImages = galleryImagesList;
        this.mContext = context;
        this.clickListener = clickListener;
        setSize(context);
    }

    // Create new views
    @Override
    public AlbumImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_album_images, null);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView, clickListener);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final int pos = position;

        Glide.with(mContext)
//                .load("file://" + mAlbumImages.get(position))
                .load(mAlbumImages.get(position))
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .crossFade()
                .into(viewHolder.imgAlbum);

        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    // Return the size arraylist
    @Override
    public int getItemCount() {
        return mAlbumImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @Bind(R.id.selected_overlay)
        RelativeLayout selectedOverlay;
        @Bind(R.id.img_album)
        ImageView imgAlbum;

        public AlbumsModel singleItem;
        private ClickListener listener;

        public ViewHolder(View view, ClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);

            imgAlbum.setLayoutParams(params);
            selectedOverlay.setLayoutParams(params);

            this.listener = listener;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (listener != null) {
                return listener.onItemLongClicked(getAdapterPosition());
            }
            return false;
        }

        public interface ClickListener {
            void onItemClicked(int position);

            boolean onItemLongClicked(int position);
        }
    }

    // method to access in activity after updating selection
    public ArrayList<String> getAlbumImagesList() {
        return mAlbumImages;
    }

    private void setSize(Context context) {
        width = context.getResources().getDisplayMetrics().widthPixels;

        width = width / AlbumImagesActivity.PHOTO_SPAN_COUNT;

        params = new RelativeLayout.LayoutParams(width, width);
    }

}
