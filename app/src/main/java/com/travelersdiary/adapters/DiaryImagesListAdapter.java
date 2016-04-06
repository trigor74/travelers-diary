package com.travelersdiary.adapters;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.DiaryImagesActivity;
import com.travelersdiary.activities.FullScreenImageActivity;
import com.travelersdiary.models.Photo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesListAdapter extends RecyclerView.Adapter<DiaryImagesListAdapter.ViewHolder> {

    private final static int IMAGES = 0;
    private final static int SHOW_ALL = 1;
    private final static int WARNING = 3;
    private final static int ITEM_COUNT = 12;

    private Fragment mFragment;
    private ArrayList<Photo> mImagesList;

    public DiaryImagesListAdapter(Fragment fragment, ArrayList<Photo> list) {
        this.mFragment = fragment;
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
        View view = LayoutInflater.from(mFragment.getContext()).inflate(R.layout.list_item_diary_note_image, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        if (viewType == IMAGES) {
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mFragment.getContext(), FullScreenImageActivity.class);
                    intent.putStringArrayListExtra("images", Utils.photoArrayToStringArray(mFragment.getContext(), mImagesList));
                    intent.putExtra("position", viewHolder.getAdapterPosition());
                    mFragment.startActivity(intent);
                }
            });
        } else if (viewType == SHOW_ALL) {
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mFragment.getContext(), DiaryImagesActivity.class);
                    intent.putExtra("images", mImagesList);
                    mFragment.startActivityForResult(intent, Constants.IMAGES_DELETE_REQUEST_CODE);
                }
            });
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.getItemViewType() == IMAGES) {
            if (Utils.checkFileExists(mFragment.getContext(), mImagesList.get(position).getLocalUri())) {
                Glide.with(mFragment.getContext())
                        .load(mImagesList.get(position).getLocalUri())
                        .centerCrop()
                        .into(holder.image);
            } else {
                Glide.with(mFragment.getContext())
                        .load(mImagesList.get(position).getPicasaUri())
                        .centerCrop()
                        .into(holder.image);
            }
        } else if (holder.getItemViewType() == SHOW_ALL) {
            holder.image.setBackgroundColor(ContextCompat.getColor(mFragment.getContext(), R.color.gray));
            Glide.with(mFragment.getContext())
                    .load(R.drawable.ic_navigate_next_white_48dp)
                    .into(holder.image);
        } else if (holder.getItemViewType() == WARNING) {
            holder.image.setBackgroundColor(ContextCompat.getColor(mFragment.getContext(), R.color.darkRed));
            holder.image.setPadding(35, 35, 35, 35);
            Glide.with(mFragment.getContext())
                    .load(R.drawable.ic_warning_white_48dp)
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if (mImagesList.size() < ITEM_COUNT) {
            return mImagesList.size() + 1;
        } else {
            return ITEM_COUNT;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemCount() - 1) {
            if (!Utils.checkFileExists(mFragment.getContext(), mImagesList.get(position).getLocalUri()) &&
                    mImagesList.get(position).getPicasaUri() == null) {
                return WARNING;
            } else {
                return IMAGES;
            }
        } else {
            return SHOW_ALL;
        }
    }

}
