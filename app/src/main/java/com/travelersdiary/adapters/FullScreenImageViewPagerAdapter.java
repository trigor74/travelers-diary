package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.travelersdiary.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoView;

public class FullScreenImageViewPagerAdapter extends PagerAdapter {

    @Bind(R.id.iv_fullscreen)
    PhotoView mImageFullScreen;

    private ArrayList<String> mImages;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public FullScreenImageViewPagerAdapter(Context context, ArrayList<String> images) {
        this.mContext = context;
        this.mImages = images;
        this.mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mLayoutInflater.inflate(R.layout.fullscreen_image, container, false);
        ButterKnife.bind(this, view);

        Glide.with(mContext)
                .load(mImages.get(position))
                .into(mImageFullScreen);

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
