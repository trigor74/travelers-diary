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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.travelersdiary.models.Photo;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

        mLayoutManager = new LinearLayoutManager(getContext());
        mDiaryList.setLayoutManager(mLayoutManager);

        // animation
        mDiaryList.setItemAnimator(new DefaultItemAnimator());

        setupAdapter();
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

    private void setupAdapter() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase userDiaryRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
        Query query;

        String travelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);
        if (travelId != null && !travelId.isEmpty()) {
            query = userDiaryRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(travelId);
        } else {
            query = userDiaryRef.orderByChild(Constants.FIREBASE_DIARY_TIME);
        }

        mAdapter = new FirebaseMultiSelectRecyclerAdapter<DiaryNote, DiaryListFragment.ViewHolder>(
                DiaryNote.class,
                R.layout.list_item_diary_note,
                DiaryListFragment.ViewHolder.class,
                query) {

            @Override
            protected void populateViewHolder(DiaryListFragment.ViewHolder holder, DiaryNote model, int position) {
                //cleanup
                holder.tvTitle.setText("");
                holder.tvDay.setText("");
                holder.tvMonth.setText("");
                holder.tvYear.setText("");
                holder.tvText.setText("");
                String img = "";
                holder.tvPhotoCount.setText("");

                //Note title
                holder.tvTitle.setText(model.getTitle());

                //Note date
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(model.getTime());
                int day = c.get(Calendar.DAY_OF_MONTH);
                String month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
                int year = c.get(Calendar.YEAR);

                holder.tvDay.setText(String.format(Locale.getDefault(), "%02d", day));
                holder.tvMonth.setText(month);
                holder.tvYear.setText(String.valueOf(year));

                //Note text
                String text = model.getText();
                if (text.isEmpty()) {
                    if (model.getPhotos() == null) {
                        holder.tvText.setVisibility(View.GONE);
                        holder.content.setVisibility(View.VISIBLE);
                    } else {
                        holder.tvText.setVisibility(View.VISIBLE);
                        holder.content.setVisibility(View.GONE);
                    }
                } else {
                    text = text.replaceAll("<.*?>", "");
                    if (text.length() > 200) {
                        text = text.substring(0, 200).concat("...");
                    }
                    holder.content.setVisibility(View.VISIBLE);
                    holder.tvText.setVisibility(View.VISIBLE);
                    holder.tvText.setText(text);
                }

                //Photo
                if (model.getPhotos() != null) {
                    int photos = model.getPhotos().size();
                    holder.tvPhotoCount.setText(String.valueOf(photos));
                    holder.layoutPhotos.setVisibility(View.VISIBLE);
                    holder.imgBackground.setVisibility(View.VISIBLE);

                    for (Photo photo : model.getPhotos()) {
                        if (Utils.checkFileExists(getContext(), photo.getLocalUri())) {
                            img = photo.getLocalUri();
                            break;
                        } else if (photo.getPicasaUri() != null) {
                            img = photo.getPicasaUri();
                            break;
                        }
                    }

                    if (!img.isEmpty()) {
                        whiteStyle(holder);
                        holder.imgBackground.setImageDrawable(null);
                        Glide.with(getContext())
                                .load(img)
                                .crossFade()
                                .into(holder.imgBackground);
                    } else {
                        holder.imgBackground.setVisibility(View.GONE);
                        blackStyle(holder);
                    }
                } else {
                    holder.tvPhotoCount.setText("0");
                    holder.layoutPhotos.setVisibility(View.GONE);
                    holder.imgBackground.setVisibility(View.GONE);
                    blackStyle(holder);
                }

                // card view selection
                holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
            }
        };

        mDiaryList.setAdapter(mAdapter);
    }

    private void blackStyle(ViewHolder holder) {
        holder.tvDay.setTextColor(getResources().getColor(R.color.black));
        holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pin_black_24dp, 0, 0, 0);
        holder.tvDay.setAlpha(.2f);
        holder.tvMonth.setTextColor(getResources().getColor(R.color.black));
        holder.tvMonth.setAlpha(.2f);
        holder.tvYear.setTextColor(getResources().getColor(R.color.black));
        holder.tvYear.setAlpha(.2f);
        holder.tvTitle.setTextColor(getResources().getColor(R.color.black));
        holder.tvTitle.setAlpha(.8f);

        holder.tvPhotoCount.setTextColor(getResources().getColor(R.color.black));
        holder.tvPhotoCount.setAlpha(.5f);
        holder.imgPhoto.setImageDrawable(null);
        holder.imgPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_library_black_24dp));
        holder.imgPhoto.setAlpha(.5f);
    }

    private void whiteStyle(ViewHolder holder) {
        holder.tvDay.setTextColor(getResources().getColor(R.color.white));
        holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pin_white_24dp, 0, 0, 0);
        holder.tvDay.setAlpha(1.0f);
        holder.tvMonth.setTextColor(getResources().getColor(R.color.white));
        holder.tvMonth.setAlpha(1.0f);
        holder.tvYear.setTextColor(getResources().getColor(R.color.white));
        holder.tvYear.setAlpha(1.0f);
        holder.tvTitle.setTextColor(getResources().getColor(R.color.white));
        holder.tvTitle.setAlpha(1.0f);

        holder.tvPhotoCount.setTextColor(getResources().getColor(R.color.white));
        holder.tvPhotoCount.setAlpha(1.0f);
        holder.imgPhoto.setImageDrawable(null);
        holder.imgPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_library_white_24dp));
        holder.imgPhoto.setAlpha(1.0f);
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
                // colorize status bar when action mode enabled
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Utils.setStatusBarColor((AppCompatActivity) view.getContext(), R.color.colorPrimaryDark);
                }
                mDeleteMode = ((AppCompatActivity) view.getContext()).startSupportActionMode(mDeleteModeCallback);
            }
            if (mDeleteMode != null) {
                mAdapter.setSelectable(true);
                mAdapter.tapSelection(position);
                if (mAdapter.getSelectedItemCount() == 0) {
                    mDeleteMode.finish();
                } else {
                    int selectedItems = mAdapter.getSelectedItemCount();
                    int items = mAdapter.getItemCount();
                    mDeleteMode.setTitle(view.getContext().getString(R.string.diary_list_action_mode_title_text, selectedItems, items));
                }
            }
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.diary_selected_overlay)
        RelativeLayout selectedOverlay;
        @Bind(R.id.diary_note_list_image)
        ImageView imgBackground;
        @Bind(R.id.diary_note_day)
        TextView tvDay;
        @Bind(R.id.diary_note_month)
        TextView tvMonth;
        @Bind(R.id.diary_note_year)
        TextView tvYear;
        @Bind(R.id.diary_note_title)
        TextView tvTitle;
        @Bind(R.id.diary_note_text)
        TextView tvText;
        @Bind(R.id.photos_layout)
        LinearLayout layoutPhotos;
        @Bind(R.id.diary_note_photo_count)
        TextView tvPhotoCount;
        @Bind(R.id.img_photo)
        ImageView imgPhoto;
        @Bind(R.id.content)
        LinearLayout content;

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
