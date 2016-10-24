package com.travelersdiary.travel;

import android.content.Context;
import android.content.Intent;
import android.databinding.Bindable;
import android.view.View;

import com.travelersdiary.BaseViewModel;
import com.travelersdiary.Constants;
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.models.Travel;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author volfor
 */
public class TravelViewModel extends BaseViewModel {

    private TravelView view;
    private Travel travel;

    public TravelViewModel(TravelView view, Travel travel) {
        this.view = view;
        this.travel = travel;
    }

    @Bindable
    public String getStart() {
        return String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getStart()),
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getStart()));
    }

    @Bindable
    public String getStop() {
        return String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getStop()),
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getStop()));
    }

    @Bindable
    public String getCreationTime() {
        return String.format("%s %s", SimpleDateFormat.getDateInstance().format(travel.getCreationTime()),
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(travel.getCreationTime()));
    }

    @Bindable
    public void onFabClick(View v) {
        switch (view.getPagerCurrentItem()) {
            case 0: // Diary Tab
                Intent diaryIntent = new Intent(v.getContext(), DiaryActivity.class);
                diaryIntent.putExtra(DiaryActivity.NEW_DIARY_NOTE, true);
                diaryIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                diaryIntent.putExtra(Constants.KEY_TRAVEL_REF, travelId);
                v.getContext().startActivity(diaryIntent);
                break;
            case 1: // Reminder Tab
                Intent remindItemIntent = new Intent(v.getContext(), ReminderItemActivity.class);
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_TITLE, travel.getTitle());
                remindItemIntent.putExtra(Constants.KEY_TRAVEL_REF, travelId);
                v.getContext().startActivity(remindItemIntent);
                break;
            case 2: // Map Tab
                break;
            default:
        }
    }

    @Override
    public void start(Context context) {

    }

    @Override
    public void stop() {

    }
}
