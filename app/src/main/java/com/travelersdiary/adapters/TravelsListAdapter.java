package com.travelersdiary.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.models.Travel;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TravelsListAdapter extends FirebaseRecyclerAdapter<Travel, TravelsListAdapter.ViewHolder> {

    public interface OnItemClickListener  {
        void onItemClick(View view, int position);
    }

    private static OnItemClickListener  onItemClickListener;

    private Context mContext;

    public void setOnItemClickListener(OnItemClickListener  onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public TravelsListAdapter(Context context, Firebase ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
        this.mContext = context;
    }

    public TravelsListAdapter(Context context, Query ref) {
        super(Travel.class, R.layout.list_item_travel, TravelsListAdapter.ViewHolder.class, ref);
        this.mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.travel_background)
        ImageView imgBackground;
        @Bind(R.id.travel_title)
        TextView tvTitle;
        @Bind(R.id.active_travel_icon)
        ImageView imgActiveTravelIcon;
        @Bind(R.id.travel_date)
        TextView tvDate;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            view.setLongClickable(true);
        }
    }

    @Override
    protected void populateViewHolder(TravelsListAdapter.ViewHolder viewHolder, Travel model, int position) {
        viewHolder.tvTitle.setText(model.getTitle());
//        viewHolder.tvTitle.setShadowLayer(1, 0, 0, Color.BLACK);

        String travelDate = getTravelDate(model.getStart(), model.getStop());
        if (travelDate != null) {
            viewHolder.tvDate.setVisibility(View.VISIBLE);
            viewHolder.tvDate.setText(travelDate);
        } else {
            viewHolder.tvDate.setVisibility(View.GONE);
        }

        if (model.isActive()) {
            viewHolder.imgActiveTravelIcon.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgActiveTravelIcon.setVisibility(View.GONE);
        }

        String cover;
        if (Utils.checkFileExists(mContext, model.getUserCover())) {
            cover = model.getUserCover();
        } else {
            cover = model.getDefaultCover();
        }

        Glide.with(mContext)
                .load(cover)
                .centerCrop()
                .crossFade()
                .into(viewHolder.imgBackground);
    }

    private String getTravelDate(long start, long end) {
        String travelDate;

        if (start == -1 && end == -1) {
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
