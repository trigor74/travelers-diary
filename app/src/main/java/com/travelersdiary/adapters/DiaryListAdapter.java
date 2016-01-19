package com.travelersdiary.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.DiaryNote;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryListAdapter extends FirebaseRecyclerAdapter<DiaryNote, DiaryListAdapter.ViewHolder> {

    public interface OnItemClickListener  {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener  onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener  onItemClickListener) {
        DiaryListAdapter.onItemClickListener = onItemClickListener;
    }

    public DiaryListAdapter(Firebase ref) {
        super(DiaryNote.class, R.layout.list_item_diary_note, DiaryListAdapter.ViewHolder.class, ref);
    }

    public DiaryListAdapter(Query ref) {
        super(DiaryNote.class, R.layout.list_item_diary_note, DiaryListAdapter.ViewHolder.class, ref);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.item_note_title_text_view)
        TextView textViewTitle;
        @Bind(R.id.item_note_time_text_view)
        TextView textViewTime;
        @Bind(R.id.item_note_travel_title_text_view)
        TextView textViewTravelTitle;
        @Bind(R.id.item_note_text_text_view)
        TextView textViewText;
        @Bind(R.id.photos_layout)
        LinearLayout layoutPhotos;
        @Bind(R.id.videos_layout)
        LinearLayout layoutVideos;
        @Bind(R.id.audios_layout)
        LinearLayout layoutAudios;
        @Bind(R.id.item_note_photos_text_view)
        TextView textViewPhotos;
        @Bind(R.id.item_note_videos_text_view)
        TextView textViewVideos;
        @Bind(R.id.item_note_audios_text_view)
        TextView textViewAudios;

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
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getLayoutPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    protected void populateViewHolder(DiaryListAdapter.ViewHolder viewHolder, DiaryNote model, int position) {
        //Note title
        viewHolder.textViewTitle.setText(model.getTitle());

        //Note time
        // String time = DateFormat.getMediumDateFormat(this).format(model.getTime())
        String time = SimpleDateFormat.getDateTimeInstance().format(model.getTime());
        viewHolder.textViewTime.setText(time);

        //Travel
        String travelTitle = model.getTravelTitle();
        String travelId = model.getTravelId();
        viewHolder.textViewTravelTitle.setText(travelTitle);
        if (travelId == null || travelId.equalsIgnoreCase(Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY)) {
            viewHolder.textViewTravelTitle.setVisibility(View.GONE);
        } else {
            viewHolder.textViewTravelTitle.setVisibility(View.VISIBLE);
        }

        //Note text
        String text = model.getText();
        text = text.replaceAll("<.*?>", "");
        if (text.length() > 200) {
            text = text.substring(0, 200).concat("...");
        }
        viewHolder.textViewText.setText(text);

        //Icons photo, video, audio
        if (model.getPhotos() != null) {
            Integer photos = model.getPhotos().size();
            viewHolder.textViewPhotos.setText(photos.toString());
            viewHolder.layoutPhotos.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewPhotos.setText("0");
            viewHolder.layoutPhotos.setVisibility(View.GONE);
        }

        if (model.getVideos() != null) {
            Integer videos = model.getVideos().size();
            viewHolder.textViewVideos.setText(videos.toString());
            viewHolder.layoutVideos.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewVideos.setText("0");
            viewHolder.layoutVideos.setVisibility(View.GONE);
        }

        if (model.getAudios() != null) {
            Integer audios = model.getAudios().size();
            viewHolder.textViewAudios.setText(audios.toString());
            viewHolder.layoutAudios.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textViewAudios.setText("0");
            viewHolder.layoutAudios.setVisibility(View.GONE);
        }
    }
}
