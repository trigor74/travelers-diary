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

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private int mNumOfTabs;
    private final List<Fragment> mFragments = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm, Context current, int numOfTabs) {
        super(fm);
        this.mContext = current;
        this.mNumOfTabs = numOfTabs;
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
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
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
