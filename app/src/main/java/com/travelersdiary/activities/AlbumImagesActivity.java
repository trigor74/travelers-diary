package com.travelersdiary.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.AlbumImagesAdapter;
import com.travelersdiary.models.AlbumsModel;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlbumImagesActivity extends AppCompatActivity
        implements AlbumImagesAdapter.ViewHolder.ClickListener {

    public static final String SELECTED_IMAGES = "selected_images";

    @Bind(R.id.album_images_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.album_images_list)
    RecyclerView mRecyclerView;

    private ActionBar mSupportActionBar;
    private AlbumImagesAdapter mAdapter;

    private ArrayList<AlbumsModel> albumsModels;
    private int mPosition;

    public ArrayList<String> mShareImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        ButterKnife.bind(this);

        mPosition = getIntent().getIntExtra("position", 0);
        albumsModels = (ArrayList<AlbumsModel>) getIntent().getSerializableExtra("albumsList");

        setSupportActionBar(mToolbar);

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
            mSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_done_white_24dp);
            setToolbarTitle();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, Constants.PHOTO_SPAN_COUNT));

        mAdapter = new AlbumImagesAdapter(this, getAlbumImages(), this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return this.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private ArrayList<String> getAlbumImages() {
        Object[] abc = albumsModels.get(mPosition).folderImages.toArray();

        Log.i("imagesLength", "" + abc.length);
        ArrayList<String> paths = new ArrayList<>();
        int size = abc.length;
        for (int i = 0; i < size; i++) {
            String albumImages = (String) abc[i];
            paths.add(albumImages);
        }

        return paths;
    }

    @Override
    public void onItemClicked(int position) {
        toggleSelection(position);
        setToolbarTitle();
    }

    @Override
    public boolean onItemLongClicked(int position) {
        toggleSelection(position);
        setToolbarTitle();

        return true;
    }

    private void setToolbarTitle() {
        int count = mShareImages.size();
        mSupportActionBar.setTitle(albumsModels.get(mPosition).getFolderName() + " (" + count + ")");
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);

        Log.i("string path", "" + mAdapter.getAlbumImagesList().get(position));

        String path = Uri.parse(mAdapter.getAlbumImagesList().get(position)).toString();
        File imageFile = new File(path);

        Uri contentUri = getImageContentUri(imageFile);

        if (contentUri == null) {
            return;
        }

        String uri = contentUri.toString();

        if (mAdapter.isSelected(position)) {
            mShareImages.add(uri);
        } else {
            mShareImages.remove(uri);
        }
        Log.i("uri path", "" + mShareImages);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putStringArrayListExtra(SELECTED_IMAGES, mShareImages);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

}
