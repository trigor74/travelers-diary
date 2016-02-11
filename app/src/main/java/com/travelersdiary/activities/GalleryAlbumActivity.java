package com.travelersdiary.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.travelersdiary.R;
import com.travelersdiary.adapters.GalleryAlbumAdapter;
import com.travelersdiary.models.AlbumsModel;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryAlbumActivity extends AppCompatActivity {

    public static final int ENTER_ALBUM_REQUEST_CODE = 22;

    @Bind(R.id.gallery_albums_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.albums_list)
    RecyclerView mRecyclerView;
    private GalleryAlbumAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<AlbumsModel> mAlbumsModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_album);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Albums");
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
//        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));

        // create an Object for Adapter
        mAdapter = new GalleryAlbumAdapter(GalleryAlbumActivity.this, getGalleryAlbumImages());

        // set the adapter object to the RecyclerView
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.SetOnItemClickListener(new GalleryAlbumAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                // do something with position

                Intent galleryAlbumsIntent = new Intent(GalleryAlbumActivity.this, AlbumImagesActivity.class);
                galleryAlbumsIntent.putExtra("position", position);
                galleryAlbumsIntent.putExtra("albumsList", getGalleryAlbumImages());
                startActivityForResult(galleryAlbumsIntent, ENTER_ALBUM_REQUEST_CODE);
            }
        });

    }

    private ArrayList<AlbumsModel> getGalleryAlbumImages() {
        final String[] columns = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imageCursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, orderBy + " DESC");
        mAlbumsModels = getAllDirectoriesWithImages(imageCursor);
        return mAlbumsModels;
    }

    public static ArrayList<AlbumsModel> getAllDirectoriesWithImages(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int size = cursor.getCount();

        TreeSet<String> folderPathList = new TreeSet<>();
        ArrayList<AlbumsModel> albumsModels = new ArrayList<>();
        HashMap<String, AlbumsModel> map = new HashMap<>();

        String imgPath, folderPath;
        AlbumsModel tempAlbumsModel;

        AlbumsModel totalAlbum = new AlbumsModel();
        totalAlbum.setFolderName("All images");
        totalAlbum.setFolderImagePath(cursor.getString(0).trim());
        albumsModels.add(totalAlbum);

        for (int i = 0; i < size; i++) {
            imgPath = cursor.getString(0).trim();
            folderPath = imgPath.substring(0, imgPath.lastIndexOf("/"));

            totalAlbum.folderImages.add(imgPath);

            if (folderPathList.add(folderPath)) {
                AlbumsModel gm = new AlbumsModel();
                String folderName = gm.getFolderName();
                String folderImagePath = gm.getFolderName();

                gm.setFolderName(folderPath.substring(
                        folderPath.lastIndexOf("/") + 1, folderPath.length()));
                gm.folderImages.add(imgPath);
                gm.setFolderImagePath(imgPath);
                albumsModels.add(gm);
                map.put(folderPath, gm);
            } else if (folderPathList.contains(folderPath)) {
                tempAlbumsModel = map.get(folderPath);
                tempAlbumsModel.folderImages.add(imgPath);
            }
            cursor.moveToNext();
        }
        return albumsModels;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENTER_ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
