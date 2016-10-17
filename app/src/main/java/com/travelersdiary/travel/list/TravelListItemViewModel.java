package com.travelersdiary.travel.list;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableBoolean;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.Travel;
import com.travelersdiary.travel.TravelActivity;

import java.util.Calendar;
import java.util.Locale;

public class TravelListItemViewModel extends BaseObservable {

    public ObservableBoolean dateVisibility = new ObservableBoolean(false);

    private String key;
    private Travel travel;

    public TravelListItemViewModel(Travel travel, String key) {
        this.travel = travel;
        this.key = key;
    }

    public void onItemClick(View v) {
        Intent intent = new Intent(v.getContext(), TravelActivity.class);
        intent.putExtra(Constants.KEY_TRAVEL_REF, key);
        intent.putExtra("Travel", travel);

        v.getContext().startActivity(intent);

//                intent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
//                intent.putExtra(Constants.KEY_TRAVEL_DESCRIPTION, travel.getDescription());
//                intent.putExtra(Constants.KEY_TRAVEL_DEFAULT_COVER, travel.getDefaultCover());
//                intent.putExtra(Constants.KEY_TRAVEL_USER_COVER, travel.getUserCover());
//                intent.putExtra(Constants.KEY_TRAVEL_IS_ACTIVE, travel.isActive());
//                intent.putExtra(Constants.KEY_TRAVEL_CREATION_TIME, travel.getCreationTime());
//                intent.putExtra(Constants.KEY_TRAVEL_START_TIME, travel.getStart());
//                intent.putExtra(Constants.KEY_TRAVEL_STOP_TIME, travel.getStop());
    }

    @Bindable
    public String getTitle() {
        return travel.getTitle();
    }

    @Bindable
    public String getDate() {
        return getTravelDate(travel.getStart(), travel.getStop());
    }

    @Bindable
    public boolean isActive() {
        return travel.isActive();
    }

    @Bindable
    public Travel getTravel() {
        return travel;
    }

    @BindingAdapter({"travelImage"})
    public static void bindTravelBackground(ImageView view, Travel travel) {
        String image;

        if (Utils.checkFileExists(view.getContext(), travel.getUserCover())) {
            image = travel.getUserCover();
        } else {
            image = travel.getDefaultCover();
        }

        Glide.with(view.getContext())
                .load(image)
                .centerCrop()
                .crossFade()
                .into(view);
    }


    private String getTravelDate(long start, long end) {
        dateVisibility.set(true);

        String travelDate;

        if (start == -1 && end == -1) {
            dateVisibility.set(false);
            return null;
        }

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(start);
        String startMonth = startCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        int startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        int startYear = startCalendar.get(Calendar.YEAR);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(end);
        String endMonth = endCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        int endDay = endCalendar.get(Calendar.DAY_OF_MONTH);
        int endYear = endCalendar.get(Calendar.YEAR);

        if (start == -1) {
            travelDate = String.format("%s - %s %s, %s", R.string.unknown, endMonth, endDay, endYear);
        } else {
            if (end == -1) {
                travelDate = String.format("%s %s, %s", startMonth, startDay, startYear);
            } else {
                if (startYear == endYear) {
                    travelDate = String.format("%s %s - %s %s, %s", startMonth, startDay, endMonth, endDay, endYear);
                } else {
                    travelDate = String.format("%s %s, %s - %s %s, %s", startMonth, startDay, startYear, endMonth, endDay, endYear);
                }
            }
        }

        return travelDate;
    }
}
