package com.travelersdiary.screens.diary;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.AlbumImagesAdapter;
import com.travelersdiary.models.Photo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesActivity extends AppCompatActivity
        implements AlbumImagesAdapter.ViewHolder.ClickListener {

    @Bind(R.id.album_images_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.album_images_list)
    RecyclerView mRecyclerView;

    public static final String IMAGES_AFTER_DELETE = "images after delete";

    private ArrayList<Photo> mImages;
    private ArrayList<String> mImagesPathList;
    private ArrayList<String> mImagesPathListPrev = new ArrayList<>();
    private String mDiaryTitle;
    private ActionBar mSupportActionBar;

    private boolean isSelectMode;

    private AlbumImagesAdapter mAdapter;
    private ArrayList<Photo> mSelectedImages = new ArrayList<>();
    private ArrayList<Photo> mImagesToDelete = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        mImages = (ArrayList<Photo>) intent.getExtras().get("images");
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

        mImagesPathList = Utils.photoArrayToStringArray(this, mImages);
        mAdapter = new AlbumImagesAdapter(this, mImagesPathList, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClicked(int position) {
        if (isSelectMode) {
            toggleSelection(position);
            setToolbarTitle();
        } else {
            Intent intent = new Intent(DiaryImagesActivity.this, FullScreenImageActivity.class);
            intent.putStringArrayListExtra("images", mImagesPathList);
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
        mSupportActionBar.setTitle(mSelectedImages.size() + "/" + mImagesPathList.size());
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

        Photo photo = mImages.get(position);

        if (mAdapter.isSelected(position)) {
            mSelectedImages.add(photo);
        } else {
            mSelectedImages.remove(photo);
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
        if (mSelectedImages.isEmpty()) {
            Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Uri> shareImages = new ArrayList<>();
        ArrayList<String> shareLinks = new ArrayList<>();
        String links = "";

        for (int i = 0; i < mSelectedImages.size(); i++) {
            if (Utils.checkFileExists(this, mSelectedImages.get(i).getLocalUri())) {
                shareImages.add(Uri.parse(mSelectedImages.get(i).getLocalUri()));
            } else if (mSelectedImages.get(i).getPicasaUri() != null) {
                shareLinks.add(mSelectedImages.get(i).getPicasaUri());
            }
        }

        for (String link : shareLinks) {
            links += link + "\n";
        }

        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_SUBJECT, mDiaryTitle);

        if (!shareImages.isEmpty()) {
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareImages);
        }

        if (!shareLinks.isEmpty()) {
            share.putExtra(Intent.EXTRA_TEXT, links);
        }

        startActivity(share);
    }

    /*very awful code, needs to be rewritten*/
    private void delete() {
        if (mSelectedImages.isEmpty()) {
            Toast.makeText(this, R.string.nothing_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        mImagesToDelete.clear();
        mImagesToDelete.addAll(mSelectedImages);

        ArrayList<Integer> positions = new ArrayList<>();

        for (int i = 0; i < mImagesPathList.size(); i++) {
            for (int j = 0; j < mImagesToDelete.size(); j++) {
                if (mImages.get(i) == mImagesToDelete.get(j)) {
                    positions.add(i);
                }
            }
        }

        ArrayList<Integer> positionsOld = new ArrayList<>();
        positionsOld.addAll(positions);

        mImagesPathListPrev.clear();
        mImagesPathListPrev.addAll(mImagesPathList);

        for (int i = 0; i < positions.size(); i++) {
            int pos = positions.get(i);
            mAdapter.notifyItemRemoved(pos);
            mImagesPathList.remove(pos);
            for (int j = 0; j < positions.size(); j++) {
                int a = positions.get(j);
                a--;
                positions.set(j, a);
            }
        }

        showSnackbar(positionsOld, mImagesToDelete);
        enableReviewMode();
    }

    private void showSnackbar(final ArrayList<Integer> positionsOld, final ArrayList<Photo> imagesToDelete) {
        int count = imagesToDelete.size();
        final ArrayList<Photo> tmp = new ArrayList<>();
        tmp.addAll(mImages);

        mImages.removeAll(imagesToDelete);

        Snackbar snackbar = Snackbar
                .make(mRecyclerView, count == 1 ? count + " PHOTO DELETED" :
                        count + " PHOTOS DELETED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (int i = 0; i < positionsOld.size(); i++) {
                            int pos = positionsOld.get(i);
                            mImagesPathList.add(pos, mImagesPathListPrev.get(pos));
                            mAdapter.notifyItemInserted(pos);
                        }
                        mImages.clear();
                        mImages.addAll(tmp);
                    }
                });
        snackbar.show();
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
                    Intent intent = new Intent();
                    intent.putExtra(IMAGES_AFTER_DELETE, mImages);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return true;
            case R.id.action_select:
                enableSelectionMode();
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_delete:
                delete();
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
            Intent intent = new Intent();
            intent.putExtra(IMAGES_AFTER_DELETE, mImages);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
