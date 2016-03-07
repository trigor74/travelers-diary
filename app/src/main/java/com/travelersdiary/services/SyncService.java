package com.travelersdiary.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.squareup.otto.Subscribe;
import com.travelersdiary.BusProvider;
import com.travelersdiary.Constants;
import com.travelersdiary.PicasaClient;
import com.travelersdiary.Utils;
import com.travelersdiary.events.OnListOfNotSyncedImagesReadyEvent;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.Photo;
import com.travelersdiary.models.Travel;
import com.travelersdiary.picasa_model.PicasaAlbum;
import com.travelersdiary.picasa_model.PicasaPhoto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends Service {

    private static final String TAG = "SyncService";

    private static int UPDATE_INTERVAL = 2;

    ArrayList<Photo> notSyncedImages = new ArrayList<>();
    final HashMap<String, ArrayList<Photo>> imagesToSync = new HashMap<>();

    private boolean isListReady = false;
    private boolean isSyncFinished = true;

    private Timer timer = new Timer();
    private boolean isRunning = false;

    private PicasaClient mPicasaClient;
    private PicasaService mPicasaService;
    private String mUserUID;

    @Override
    public void onCreate() {

        BusProvider.bus().register(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mGoogleToken = sharedPreferences.getString(Constants.KEY_USER_GOOGLE_TOKEN, null);
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        mPicasaClient = PicasaClient.getInstance();

        mPicasaClient.createService(mGoogleToken);
        mPicasaService = mPicasaClient.getPicasaService();

        Log.i(TAG, "Service onCreate\n" +
                "1 Picasa client " + mPicasaClient + "\n" +
                "1 Picasa service " + mPicasaService + "\n" +
                "--- Google token " + mGoogleToken);

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand\n" +
                "2 Picasa client " + mPicasaClient + "\n" +
                "2 Picasa service " + mPicasaService);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {
                    Log.i(TAG, "Service running\n" +
                            "3 Picasa client " + mPicasaClient + "\n" +
                            "3 Picasa service " + mPicasaService);

                    if (isSyncFinished) {
                        getListOfNotSyncedImages();
//                        isSyncFinished = false;
                    }

                    //uploadPhoto();
                }
            }
        }, 10000, UPDATE_INTERVAL * 60000);

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        BusProvider.bus().unregister(this);
        Log.i(TAG, "Service onDestroy");
    }

    private void getListOfNotSyncedImages() {
//        final ArrayList<Photo> notSyncedImages = new ArrayList<>();
//        final HashMap<String, ArrayList<Photo>> imagesToSync = new HashMap<>();

        new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            DiaryNote diaryNote = child.getValue(DiaryNote.class);

                            ArrayList<Photo> photos = diaryNote.getPhotos(); //TODO photos null

                            if (photos != null) {
                                for (int i = 0; i < photos.size(); i++) {
                                    if (photos.get(i).getPicasaUri() == null) {
                                        notSyncedImages.add(photos.get(i));
                                        imagesToSync.put(diaryNote.getTravelId(), notSyncedImages);
                                    }
                                }
                            }
                        }

                        if (!imagesToSync.isEmpty()) {
                            isSyncFinished = false;
                            BusProvider.bus().post(new OnListOfNotSyncedImagesReadyEvent());
                        } else {
                            isSyncFinished = true;
                            Log.e(TAG, "Nothing to sync");
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    @Subscribe
    public void onListOfNotSyncedImagesReady(OnListOfNotSyncedImagesReadyEvent event) {
        uploadPhotosToPicasa(imagesToSync);
    }


    private void uploadPhotosToPicasa(HashMap<String, ArrayList<Photo>> imagesToUpload) {

        for (Map.Entry<String, ArrayList<Photo>> entry : imagesToUpload.entrySet()) {

            final String key = entry.getKey(); // travelId
            final ArrayList<Photo> value = entry.getValue();

            new Firebase(Utils.getFirebaseUserTravelsUrl(mUserUID))
                    .child(key)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Travel travel = dataSnapshot.getValue(Travel.class);

                            if (travel.getPicasaAlbumId() == null) {
                                //TODO: create picasa album and upload photos to it
                                createAlbumAndUploadPhotos(travel, key, value);
                            } else {
                                //TODO: upload photos

                                for (Photo photo : value) {
                                    uploadPhoto(photo, key, travel.getPicasaAlbumId());
                                }

                                isSyncFinished = true;
                                notSyncedImages.clear();
                                imagesToSync.clear();

                                Log.e(TAG, "Sync finished, arrays cleared");
                            }

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });
        }
    }


    private void createAlbumAndUploadPhotos(Travel travel, final String travelId, final ArrayList<Photo> photos) {
//        PicasaAlbum album = new PicasaAlbum();
//        album.setTitle("My New Album2");

        mPicasaClient.getPicasaService()
                .createAlbum("113984660589503350851", new PicasaAlbum(travel.getTitle()))
                .enqueue(new Callback<PicasaAlbum>() {
                    @Override
                    public void onResponse(Call<PicasaAlbum> call, final Response<PicasaAlbum> response) {
                        if (response.isSuccess()) {
                            //TODO:write albumId to travel and travel's diaries;

                            final String albumId = response.body().getAlbumId();

                            //update travel
                            Firebase firebaseRef = new Firebase(Utils.getFirebaseUserTravelsUrl(mUserUID));
                            Map<String, Object> map = new HashMap<>();
                            map.put(Constants.FIREBASE_PICASA_ALABUM_ID, albumId);
                            Firebase editTravelRef = firebaseRef.child(travelId);
                            editTravelRef.updateChildren(map);

                            //update all diaries of this travel
                            new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                                    .orderByChild(Constants.FIREBASE_DIARY_TRAVELID)
                                    .equalTo(travelId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Map<String, Object> map = new HashMap<>();
                                            map.put(Constants.FIREBASE_PICASA_ALABUM_ID, albumId);
                                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                child.getRef().updateChildren(map);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                        }
                                    });

                            //upload images
                            for (Photo photo : photos) {
                                uploadPhoto(photo, travelId, albumId);
                            }

                            Log.d(TAG, "Picasa create album onResponse: isSuccess");

                            isSyncFinished = true;
                            notSyncedImages.clear();
                            imagesToSync.clear();

                            Log.e(TAG, "Sync finished, arrays cleared");
                        } else {
                            int statusCode = response.code();
                            ResponseBody errorBody = response.errorBody();
                            try {
                                Log.d(TAG, "Picasa create album onResponse: " + errorBody.string() + statusCode);
                            } catch (IOException e) {
                                Log.d(TAG, "Picasa create album onResponse: failed");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PicasaAlbum> call, Throwable t) {
                        if (t != null && t.getMessage() != null) {
                            Log.d(TAG, "Picasa create album onFailure: " + t.getMessage());
                        } else {
                            Log.d(TAG, "Picasa create album onFailure: failed");
                        }
                    }
                });
    }

    private void uploadPhoto(final Photo photo, final String travelId, final String albumId) {
        final MediaType IMAGE = MediaType.parse("image/jpeg");

        Uri uri = Uri.parse(photo.getLocalUri());
        String path = Utils.getRealPathFromURI(this, uri);
        File file = new File(path);

        RequestBody requestBody = RequestBody.create(IMAGE, file);

        mPicasaService.uploadPhoto("113984660589503350851",
                albumId, requestBody).enqueue(new Callback<PicasaPhoto>() {
            @Override
            public void onResponse(Call<PicasaPhoto> call, final Response<PicasaPhoto> response) {
                if (response.isSuccess()) {
                    Log.d(TAG, "Picasa upload photo onResponse: isSuccess");

                    new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                            .orderByChild(Constants.FIREBASE_DIARY_TRAVELID)
                            .equalTo(travelId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                                        DiaryNote diaryNote = child.getValue(DiaryNote.class);

                                        //TODO update firebase picasa uri
                                        if (diaryNote.getPicasaAlbumId().equals(albumId)) { //TODO albumId
                                            ArrayList<Photo> remotePhotoList = diaryNote.getPhotos();

                                            for (int i = 0; i < remotePhotoList.size(); i++) {
                                                if (remotePhotoList.get(i).getLocalUri()
                                                        .equals(photo.getLocalUri())) {
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put(Constants.FIREBASE_PICASA_URI,
                                                            response.body().getSrc()); //TODO picasaUri

                                                    child.getRef().child("photos")
                                                            .child(String.valueOf(i)).updateChildren(map); //TODO return
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                }
                            });
                } else {
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    try {
                        Log.d(TAG, "Picasa upload photo onResponse: " + errorBody.string() + statusCode);
                    } catch (IOException e) {
                        Log.d(TAG, "Picasa upload photo onResponse: failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<PicasaPhoto> call, Throwable t) {
                if (t != null && t.getMessage() != null) {
                    Log.d(TAG, "Picasa upload photo onFailure: " + t.getMessage());
                } else {
                    Log.d(TAG, "Picasa upload photo onFailure: failed");
                }
            }
        });
    }
}
