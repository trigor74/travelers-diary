package com.travelersdiary.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.FullScreenImageViewPagerAdapter;
import com.travelersdiary.models.Photo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FullScreenImageActivity extends AppCompatActivity {

    @Bind(R.id.full_screen_image_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.vp_image)
    ViewPager mViewPager;

    private ActionBar mSupportActionBar;

    private ArrayList<Photo> mImages;
    private int mPosition;

    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (mViewPager != null) {
                mViewPager.setCurrentItem(position);
                setActionBarTitle(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mImages = (ArrayList<Photo>) extras.get("images");
        mPosition = extras.getInt("position");

        setSupportActionBar(mToolbar);

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSupportActionBar.hide();

        setupViewPager();
        setActionBarTitle(mPosition);
    }

    private void setupViewPager() {
        mViewPager.setAdapter(new FullScreenImageViewPagerAdapter(this, Utils.photoArrayToStringArray(this, mImages)));
        mViewPager.setPageMargin(40);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mViewPager.setCurrentItem(mPosition);
    }

    private void setActionBarTitle(int position) {
        if (mViewPager != null && mImages.size() > 1) {
            int totalPages = mViewPager.getAdapter().getCount();
            mSupportActionBar.setTitle(String.format("%d of %d", (position + 1), totalPages));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

}
