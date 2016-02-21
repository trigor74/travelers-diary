package com.travelersdiary.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.AlbumImagesAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesActivity extends AppCompatActivity
        implements AlbumImagesAdapter.ViewHolder.ClickListener {

    @Bind(R.id.album_images_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.album_images_list)
    RecyclerView mRecyclerView;

    private ArrayList<String> mImages;
    private String mDiaryTitle;
    private ActionBar mSupportActionBar;

    private boolean isSelectMode;

    private AlbumImagesAdapter mAdapter;
    private ArrayList<String> mSelectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mImages = intent.getStringArrayListExtra("images");
        mDiaryTitle = intent.getStringExtra("title");

        setSupportActionBar(mToolbar);

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Constants.PHOTO_SPAN_COUNT));

        mAdapter = new AlbumImagesAdapter(this, mImages, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClicked(int position) {
        if (isSelectMode) {
            toggleSelection(position);
            setToolbarTitle();
        } else {
            Intent intent = new Intent(DiaryImagesActivity.this, FullScreenImageActivity.class);
            intent.putStringArrayListExtra("images", mImages);
            intent.putExtra("position", position);

            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (!isSelectMode) {
            enableSelectionMode();
        }

        toggleSelection(position);
        setToolbarTitle();
        return true;
    }

    private void setToolbarTitle() {
        mSupportActionBar.setTitle(mSelectedImages.size() + "/" + mImages.size());
    }

    private void enableReviewMode() {
        isSelectMode = false;
        mSelectedImages.clear();
        mAdapter.clearSelection();
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void enableSelectionMode() {
        isSelectMode = true;
        setToolbarTitle();
        mSupportActionBar.invalidateOptionsMenu();
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);

        Log.i("string path", "" + mAdapter.getAlbumImagesList().get(position));

        String uri = mImages.get(position);

        if (mAdapter.isSelected(position)) {
            mSelectedImages.add(uri);
        } else {
            mSelectedImages.remove(uri);
        }
        Log.i("uri path", "" + mSelectedImages);
    }

    private void selectAll() {
        for (int i = 0; i < mImages.size(); i++) {
            if (!mAdapter.isSelected(i)) {
                toggleSelection(i);
            }
        }
        setToolbarTitle();
    }

    private void share() {
        ArrayList<Uri> shareImages = new ArrayList<>();

        if (mSelectedImages.size() > 0) {
            for (int i = 0; i < mSelectedImages.size(); i++) {
                Uri uri = Uri.parse(mSelectedImages.get(i));
                shareImages.add(uri);
            }

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, mDiaryTitle);
            intent.setType("image/jpeg");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareImages);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isSelectMode) {
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
            menu.setGroupVisible(R.id.review_mode_menu, false);
            menu.setGroupVisible(R.id.select_mode_menu, true);
        } else {
            mSupportActionBar.setTitle(R.string.images);
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            menu.setGroupVisible(R.id.select_mode_menu, false);
            menu.setGroupVisible(R.id.review_mode_menu, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_images_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isSelectMode) {
                    enableReviewMode();
                } else {
                    finish();
                }
                return true;
            case R.id.action_select:
                enableSelectionMode();
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_select_all:
                selectAll();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isSelectMode) {
            enableReviewMode();
        } else {
            super.onBackPressed();
        }
    }

}
