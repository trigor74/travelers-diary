package com.travelersdiary.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.adapters.FirebaseMultiSelectRecyclerAdapter;
import com.travelersdiary.interfaces.IActionModeFinishCallback;
import com.travelersdiary.interfaces.IOnItemClickListener;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryListFragment extends Fragment implements IActionModeFinishCallback {

    @Bind(R.id.diary_list)
    RecyclerView mDiaryList;

    private static FirebaseMultiSelectRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static ActionMode mDeleteMode = null;
    private static ActionMode.Callback mDeleteModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.diary_list_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_item_delete) {
                for (Firebase ref :
                        (List<Firebase>) mAdapter.getSelectedItemsRef()) {
                    ref.removeValue();
                }
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.setSelectable(false);
            mAdapter.clearSelections();
            mDeleteMode = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mDiaryList.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayoutManager = new LinearLayoutManager(getContext());
        mDiaryList.setLayoutManager(mLayoutManager);

        // animation
        mDiaryList.setItemAnimator(new DefaultItemAnimator());

        // decoration
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext());
        mDiaryList.addItemDecoration(itemDecoration);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase mFirebaseRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
        Query query;

        String travelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_KEY);
        if (travelId != null && !travelId.isEmpty()) {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(travelId);
        } else {
            query = mFirebaseRef.orderByChild(Constants.FIREBASE_DIARY_TIME);
        }

        mAdapter = new FirebaseMultiSelectRecyclerAdapter<DiaryNote, DiaryListFragment.ViewHolder>(
                DiaryNote.class,
                R.layout.list_item_diary_note,
                DiaryListFragment.ViewHolder.class,
                query) {

            @Override
            protected void populateViewHolder(DiaryListFragment.ViewHolder viewHolder, DiaryNote model, int position) {
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
                    viewHolder.textViewPhotos.setText(String.valueOf(photos));
                    viewHolder.layoutPhotos.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.textViewPhotos.setText("0");
                    viewHolder.layoutPhotos.setVisibility(View.GONE);
                }

                if (model.getVideos() != null) {
                    Integer videos = model.getVideos().size();
                    viewHolder.textViewVideos.setText(String.valueOf(videos));
                    viewHolder.layoutVideos.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.textViewVideos.setText("0");
                    viewHolder.layoutVideos.setVisibility(View.GONE);
                }

                if (model.getAudios() != null) {
                    Integer audios = model.getAudios().size();
                    viewHolder.textViewAudios.setText(String.valueOf(audios));
                    viewHolder.layoutAudios.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.textViewAudios.setText("0");
                    viewHolder.layoutAudios.setVisibility(View.GONE);
                }
            }
        }
        ;

        mDiaryList.setAdapter(mAdapter);
    }

    @Override
    public void finishActionMode() {
        if (mDeleteMode != null) {
            mDeleteMode.finish();
        }
    }

    @Override
    public void onPause() {
        finishActionMode();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        mAdapter.cleanup();
        super.onDestroyView();
    }

    private static IOnItemClickListener onItemClickListener = new IOnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (!mAdapter.tapSelection(position)) {
                String key = mAdapter.getRef(position).getKey();

                Intent intent = new Intent(view.getContext(), DiaryActivity.class);
                intent.putExtra(Constants.KEY_DAIRY_NOTE_REF, key);
                view.getContext().startActivity(intent);
            } else {
                if (mDeleteMode != null) {
                    if (mAdapter.getSelectedItemCount() == 0) {
                        mDeleteMode.finish();
                    } else {
                        int selectedItems = mAdapter.getSelectedItemCount();
                        int items = mAdapter.getItemCount();
                        mDeleteMode.setTitle(view.getContext().getString(R.string.diary_list_action_mode_title_text, selectedItems, items));
                    }
                }
            }
        }

        @Override
        public void onItemLongClick(View view, int position) {
            if (mDeleteMode == null) {
                mDeleteMode = ((AppCompatActivity) view.getContext()).startSupportActionMode(mDeleteModeCallback);
            }
            if (mDeleteMode != null) {
                mAdapter.setSelectable(true);
                mAdapter.setSelected(position, true);

                int selectedItems = mAdapter.getSelectedItemCount();
                int items = mAdapter.getItemCount();
                mDeleteMode.setTitle(view.getContext().getString(R.string.diary_list_action_mode_title_text, selectedItems, items));
            }
        }
    };

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
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getAdapterPosition());
                    }
                }
            });

            view.setLongClickable(true);
            view.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getAdapterPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }
}
