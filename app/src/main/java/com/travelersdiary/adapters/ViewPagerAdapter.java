package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.travelersdiary.R;
import com.travelersdiary.activities.MainActivity;
import com.travelersdiary.activities.TravelActivity;
import com.travelersdiary.fragments.DiaryListFragment;
import com.travelersdiary.fragments.MapFragment;
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
                if (mContext.getClass().equals(MainActivity.class)) {
                    return new TravelsListFragment();
                } else if (mContext.getClass().equals(TravelActivity.class)) {
                    return new DiaryListFragment();
                } else {
                    return null;
                }
            case 1:
                if (mContext.getClass().equals(MainActivity.class)) {
                    return new DiaryListFragment();
                } else if (mContext.getClass().equals(TravelActivity.class)) {
                    return new ReminderListFragment();
                } else {
                    return null;
                }
            case 2:
                if (mContext.getClass().equals(MainActivity.class)) {
                    return new ReminderListFragment();
                } else if (mContext.getClass().equals(TravelActivity.class)) {
                    return new MapFragment();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
