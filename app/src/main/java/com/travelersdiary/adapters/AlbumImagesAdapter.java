package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.travelersdiary.Constants;
import com.travelersdiary.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlbumImagesAdapter extends SelectableAdapter<AlbumImagesAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mAlbumImages;
    private ViewHolder.ClickListener clickListener;

    private static RelativeLayout.LayoutParams mParams;

    public AlbumImagesAdapter(Context context, ArrayList<String> galleryImagesList,
                              ViewHolder.ClickListener clickListener) {
        this.mAlbumImages = galleryImagesList;
        this.mContext = context;
        this.clickListener = clickListener;
        setSize(context);
    }

    @Override
    public AlbumImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_album_images, null);

        return new ViewHolder(itemLayoutView, clickListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        Glide.with(mContext)
                .load(mAlbumImages.get(position))
                .centerCrop()
                .placeholder(R.drawable.image_loading)
                .crossFade()
                .into(viewHolder.imgAlbum);

        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mAlbumImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        @Bind(R.id.selected_overlay)
        RelativeLayout selectedOverlay;
        @Bind(R.id.img_album)
        ImageView imgAlbum;

        private ClickListener listener;

        public ViewHolder(View view, ClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);

            imgAlbum.setLayoutParams(mParams);
            selectedOverlay.setLayoutParams(mParams);

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

    public ArrayList<String> getAlbumImagesList() {
        return mAlbumImages;
    }

    private void setSize(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        width = width / Constants.PHOTO_SPAN_COUNT;

        mParams = new RelativeLayout.LayoutParams(width, width);
    }

}
