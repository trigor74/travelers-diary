package com.travelersdiary.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.AlbumImagesAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DiaryImagesActivity extends AppCompatActivity implements AlbumImagesAdapter.ViewHolder.ClickListener {

    public static int PHOTO_SPAN_COUNT = 3;

    @Bind(R.id.album_images_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.album_images_list)
    RecyclerView mRecyclerView;

    private ActionBar mSupportActionBar;
    private AlbumImagesAdapter mAdapter;

    private ArrayList<String> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_images);
        ButterKnife.bind(this);

        mImages = getIntent().getStringArrayListExtra("images");

        setSupportActionBar(mToolbar);

        mSupportActionBar = getSupportActionBar();
        if (mSupportActionBar != null) {
            mSupportActionBar.setDisplayHomeAsUpEnabled(true);
//            mSupportActionBar.setTitle(albumsModels.get(mPosition).getFolderName());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, PHOTO_SPAN_COUNT));

        // create an Object for Adapter
        mAdapter = new AlbumImagesAdapter(this, mImages, this);

        // set the adapter object to the RecyclerView
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onItemClicked(int position) {
//        Intent intent = new Intent(DiaryImagesActivity.this, FullScreenImageActivity.class);
        Intent intent = new Intent(DiaryImagesActivity.this, FullScreenImageActivity.class);
        intent.putStringArrayListExtra("images", mImages);
        intent.putExtra("position", position);

        startActivity(intent);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        Toast.makeText(this, "item" + position + "long clicked", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

}
