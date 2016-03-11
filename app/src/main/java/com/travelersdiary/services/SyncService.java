package com.travelersdiary.services;

import android.app.Service;
import android.content.Context;
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
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.travelersdiary.Constants;
import com.travelersdiary.PicasaClient;
import com.travelersdiary.Utils;
import com.travelersdiary.interfaces.PicasaService;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.Photo;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.picasa.PicasaAlbum;
import com.travelersdiary.models.picasa.PicasaPhoto;

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

    private static int SYNC_INTERVAL;

    private Timer timer = new Timer();
    private boolean isRunning = false;

    private int photosToUpload = 0;

    private PicasaClient mPicasaClient;
    private PicasaService mPicasaService;

    private String mUserGoogleId;
    private String mUserUID;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SYNC_INTERVAL = Integer.valueOf(sharedPreferences.getString("sync_interval", "30"));
        boolean enabled = sharedPreferences.getBoolean("sync_service_check_box", true);

        Log.i(TAG, "*****Shared preferences***** \n" +
                "SYNC_INTERVAL: " + SYNC_INTERVAL + "\n" +
                "enabled: " + enabled);

        mPicasaClient = PicasaClient.getInstance();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand\n" +
                "1 Picasa client " + mPicasaClient + "\n" +
                "1 Picasa service " + mPicasaService);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isRunning) {

                    getToken(SyncService.this);

                    Log.i(TAG, "Service running\n" +
                            "3 Picasa client " + mPicasaClient + "\n" +
                            "3 Picasa service " + mPicasaService);

                    if (photosToUpload == 0) {
                        getListOfNotSyncedImages();
                    }
                }
            }
        }, 10000, SYNC_INTERVAL * 60000);

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPreferences.getBoolean("sync_service_check_box", true);
        Log.i(TAG, "Service onDestroy \n" +
                "enabled: " + enabled);
    }

    private void getToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = sharedPreferences.getString(Constants.KEY_EMAIL, null);
        mUserGoogleId = sharedPreferences.getString(Constants.KEY_USER_GOOGLE_ID, null);
        mUserUID = sharedPreferences.getString(Constants.KEY_USER_UID, null); //firebase userId

        try {
            String scope = String.format("oauth2:%s", "https://picasaweb.google.com/data/");
            String token = null;
            if (email != null) {
                token = GoogleAuthUtil.getToken(context, email, scope);
            }

            Log.i(TAG, "Get Token\n" +
                    "2 Picasa client " + mPicasaClient + "\n" +
                    "2 Picasa service " + mPicasaService + "\n" +
                    "--- Google token " + token);

            mPicasaClient.createService(token);
            mPicasaService = mPicasaClient.getPicasaService();
        } catch (IOException | GoogleAuthException e) {
            e.printStackTrace();
        }
    }

    private void getListOfNotSyncedImages() {
        final HashMap<String, ArrayList<Photo>> imagesToSync = new HashMap<>();

        new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            DiaryNote diaryNote = child.getValue(DiaryNote.class);

                            ArrayList<Photo> photos = diaryNote.getPhotos();
                            ArrayList<Photo> notSyncedImages;

                            if (imagesToSync.containsKey(diaryNote.getTravelId())) {
                                notSyncedImages = imagesToSync.get(diaryNote.getTravelId());
                            } else {
                                notSyncedImages = new ArrayList<>();
                            }

                            if (photos != null) {
                                for (int i = 0; i < photos.size(); i++) {
                                    if (photos.get(i).getPicasaUri() == null) {
                                        notSyncedImages.add(photos.get(i));
                                        imagesToSync.put(diaryNote.getTravelId(), notSyncedImages);
                                        photosToUpload++; //count of photos for upload to picasa
                                    }
                                }
                            }
                        }

                        if (!imagesToSync.isEmpty()) {
                            Log.e(TAG, "Photos to upload: " + photosToUpload);
                            uploadPhotosToPicasa(imagesToSync);
                        } else {
                            Log.e(TAG, "Photos to upload: " + photosToUpload);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                    }
                });
    }

    private void uploadPhotosToPicasa(HashMap<String, ArrayList<Photo>> imagesToUpload) {
        for (Map.Entry<String, ArrayList<Photo>> entry : imagesToUpload.entrySet()) {
            final String travelId = entry.getKey(); // travelId
            final ArrayList<Photo> photos = entry.getValue(); //array of photos

            new Firebase(Utils.getFirebaseUserTravelsUrl(mUserUID))
                    .child(travelId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Travel travel = dataSnapshot.getValue(Travel.class);

                            if (travel.getPicasaAlbumId() == null) {
                                createAlbumAndUploadPhotos(travel, travelId, photos);
                            } else {
                                for (Photo photo : photos) {
                                    uploadPhoto(photo, travelId, travel.getPicasaAlbumId());
                                }
//                                imagesToSync.clear();
//                                Log.e(TAG, "imagesToSync map cleared");
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                        }
                    });
        }
    }

    private void createAlbumAndUploadPhotos(Travel travel, final String travelId, final ArrayList<Photo> photos) {
        mPicasaService.createAlbum(mUserGoogleId, new PicasaAlbum(travel.getTitle())).enqueue(new Callback<PicasaAlbum>() {
            @Override
            public void onResponse(Call<PicasaAlbum> call, final Response<PicasaAlbum> response) {
                if (response.isSuccess()) {
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

                    Log.d(TAG, "Picasa create album onResponse: isSuccess");

                    //upload images
                    for (Photo photo : photos) {
                        uploadPhoto(photo, travelId, albumId);
                    }

//                    imagesToSync.clear();
//                    Log.e(TAG, "imagesToSync map cleared");
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

    private void uploadPhoto(final Photo photo, final String travelId, String albumId) {
        MediaType IMAGE = MediaType.parse("image/jpeg");

        Uri uri = Uri.parse(photo.getLocalUri());
        String path = Utils.getRealPathFromURI(this, uri);
        File file = new File(path);

        if (!file.exists()) {
            photosToUpload--;
            Log.e(TAG, "--------> File is not exists!!! Photos to upload: " + photosToUpload);
            return;
        }

        RequestBody requestBody = RequestBody.create(IMAGE, file);

        mPicasaService.uploadPhoto(mUserGoogleId, albumId, requestBody).enqueue(new Callback<PicasaPhoto>() {
            @Override
            public void onResponse(Call<PicasaPhoto> call, final Response<PicasaPhoto> response) {
                if (response.isSuccess()) {
                    Log.d(TAG, "Picasa upload photo onResponse: isSuccess");

                    photosToUpload--;
                    Log.e(TAG, "Photos to upload: " + photosToUpload);

                    new Firebase(Utils.getFirebaseUserDiaryUrl(mUserUID))
                            .orderByChild(Constants.FIREBASE_DIARY_TRAVELID)
                            .equalTo(travelId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        DiaryNote diaryNote = child.getValue(DiaryNote.class);

                                        ArrayList<Photo> remotePhotoList = diaryNote.getPhotos();
                                        if (remotePhotoList != null) {
                                            for (int i = 0; i < remotePhotoList.size(); i++) {
                                                if (remotePhotoList.get(i).getLocalUri()
                                                        .equals(photo.getLocalUri())) {
                                                    Map<String, Object> map = new HashMap<>();

                                                    map.put(Constants.FIREBASE_PICASA_URI,
                                                            response.body().getSrc());

                                                    //update picasa uri of photo
                                                    child.getRef()
                                                            .child("photos")
                                                            .child(String.valueOf(i))
                                                            .updateChildren(map);
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
