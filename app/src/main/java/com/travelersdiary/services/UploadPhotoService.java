package com.travelersdiary.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.travelersdiary.Constants;
import com.travelersdiary.Utils;
import com.travelersdiary.models.Photo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Volfor on 24.05.2017.
 * http://github.com/Volfor
 */
public class UploadPhotoService extends IntentService {
    public static final String EXTRA_REF = "ref";
    public static final String EXTRA_IMAGES = "images";
    public static final String ACTION_DIARY = "diary_images";
    public static final String ACTION_TRAVEL = "travel_cover";

    public UploadPhotoService() {
        super("UploadPhotoService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        String refString = null;
        try {
            refString = URLDecoder.decode(intent.getStringExtra(EXTRA_REF), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (refString == null) return;

        final Firebase itemRef = new Firebase(refString);
        InputStream stream;

        switch (intent.getAction()) {
            case ACTION_TRAVEL:
                String cover = intent.getStringExtra(EXTRA_IMAGES);
                if (cover == null) return;

                try {
                    stream = getContentResolver().openInputStream(Uri.parse(cover));
                } catch (Exception e) {
                    Log.e(UploadPhotoService.class.getCanonicalName(), e.getMessage(), e);
                    return;
                }

                if (stream == null) return;
                String coverName = cover.substring(cover.lastIndexOf('/') + 1);
                StorageReference coverStorageRef = FirebaseStorage.getInstance()
                        .getReference()
                        .child(itemRef.getPath().toString())
                        .child(coverName);

                UploadTask coverUploadTask = coverStorageRef.putStream(stream);
                coverUploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(UploadPhotoService.class.getCanonicalName(), e.getMessage(), e);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests")
                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        Log.d(UploadPhotoService.class.getCanonicalName(), "Success:" + downloadUrl);

                        if (downloadUrl != null) {
                            itemRef.child(Constants.FIREBASE_TRAVEL_USER_COVER).setValue(downloadUrl.toString());
                        }
                    }
                });
                break;
            case ACTION_DIARY:
                ArrayList<Photo> images = (ArrayList<Photo>) intent.getSerializableExtra(EXTRA_IMAGES);
                for (final Photo image : images) {
                    if (!TextUtils.isEmpty(image.getPicasaUri())) continue;

                    String path = image.getLocalUri();

                    try {
                        String realPath = Utils.getRealPathFromURI(getApplicationContext(), Uri.parse(path));
                        if (realPath == null) continue;
                        stream = new FileInputStream(realPath);
                    } catch (FileNotFoundException e) {
                        Log.e(UploadPhotoService.class.getCanonicalName(), e.getMessage(), e);
                        continue;
                    }

                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    StorageReference storage = FirebaseStorage.getInstance()
                            .getReference()
                            .child(itemRef.getPath().toString())
                            .child("images")
                            .child(new Date().getTime() + "_" + filename);

                    UploadTask uploadTask = storage.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(UploadPhotoService.class.getCanonicalName(), e.getMessage(), e);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            Log.d(UploadPhotoService.class.getCanonicalName(), "Success:" + downloadUrl);

                            if (downloadUrl != null) {
                                itemRef.child(Constants.FIREBASE_DIARY_PHOTOS)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                GenericTypeIndicator<List<Photo>> t =
                                                        new GenericTypeIndicator<List<Photo>>() {
                                                        };

                                                List<Photo> diaryImages = dataSnapshot.getValue(t);

                                                if (diaryImages != null) {
                                                    for (int i = 0; i < diaryImages.size(); i++) {
                                                        if (diaryImages.get(i).getLocalUri()
                                                                .equals(image.getLocalUri())) {

                                                            Map<String, Object> map = new HashMap<>();
                                                            map.put(Constants.FIREBASE_PICASA_URI,
                                                                    downloadUrl.toString());

                                                            itemRef.child(Constants.FIREBASE_DIARY_PHOTOS)
                                                                    .child(String.valueOf(i))
                                                                    .updateChildren(map);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                            }
                                        });
                            }
                        }
                    });
                }
                break;
            default:
        }
    }
}
