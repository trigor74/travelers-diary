package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.travelersdiary.R;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.ReminderListFragment;
import com.travelersdiary.fragments.TravelsListFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private int mNumOfTabs;

    public ViewPagerAdapter(FragmentManager fm, Context current, int numOfTabs) {
        super(fm);
        this.mContext = current;
        this.mNumOfTabs = numOfTabs;
    }

    // Overriding the getWidth method
    @Override
    public float getPageWidth(int position) {
        float nbPages;
        boolean isTabletLand = mContext.getResources().getBoolean(R.bool.isTabletLand);
        // Check the device
        if (isTabletLand) {
            nbPages = 2;     // 2 fragments / pages
        } else {
            nbPages = 1;     // 1 fragment / pages
        }
        return (1 / nbPages);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TravelsListFragment();
            case 1:
                return new DiaryListFragment();
            case 2:
                return new ReminderListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
