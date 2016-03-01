package com.travelersdiary.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.adapters.GalleryAlbumAdapter;
import com.travelersdiary.fragments.NoItemsFragment;
import com.travelersdiary.models.AlbumsModel;
import com.travelersdiary.recyclerview.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryAlbumActivity extends AppCompatActivity {

    @Bind(R.id.gallery_albums_activity_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.albums_list)
    RecyclerView mRecyclerView;

    @Bind(R.id.fragment_no_items_container)
    LinearLayout mNoItemsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_album);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Utils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        ArrayList<AlbumsModel> albumsModels = getGalleryAlbumImages();

        if (albumsModels == null) {
            mNoItemsContainer.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_no_items_container, new NoItemsFragment())
                    .commit();
            return;
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));

        GalleryAlbumAdapter adapter = new GalleryAlbumAdapter(GalleryAlbumActivity.this, albumsModels);
        mRecyclerView.setAdapter(adapter);

        adapter.SetOnItemClickListener(new GalleryAlbumAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                Intent galleryAlbumsIntent = new Intent(GalleryAlbumActivity.this, AlbumImagesActivity.class);
                galleryAlbumsIntent.putExtra("position", position);
                galleryAlbumsIntent.putExtra("albumsList", getGalleryAlbumImages());
                startActivityForResult(galleryAlbumsIntent, Constants.ENTER_ALBUM_REQUEST_CODE);
            }
        });

    }

    private ArrayList<AlbumsModel> getGalleryAlbumImages() {
        final String[] columns = {MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imageCursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                null, null, orderBy + " DESC");
        return getAllDirectoriesWithImages(imageCursor);
    }

    public static ArrayList<AlbumsModel> getAllDirectoriesWithImages(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int size = cursor.getCount();

        if (size == 0) {
            return null;
        }

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
        if (requestCode == Constants.ENTER_ALBUM_REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
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

    @Override
    protected void onDestroy() {
        Utils.clearImageCache(this); // clears all glide cache
        super.onDestroy();
    }

}
