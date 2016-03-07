package com.travelersdiary;

import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.travelersdiary.events.PicasaEvent;
import com.travelersdiary.picasa_model.PicasaAlbum;
import com.travelersdiary.picasa_model.PicasaFeed;
import com.travelersdiary.picasa_model.PicasaPhoto;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiRequestHandler {

    private Bus mBus;
    private PicasaClient mPicasaClient;

    public ApiRequestHandler(Bus bus) {
        mBus = bus;
        mPicasaClient = PicasaClient.getInstance();
    }

    @Subscribe
    public void onLoadingAlbumsStart(PicasaEvent.OnLoadingStart onLoadingStart) {
        mPicasaClient.getPicasaService().feed(onLoadingStart.getUsername())
                .enqueue(new Callback<PicasaFeed>() {

                    @Override
                    public void onResponse(Call<PicasaFeed> call, Response<PicasaFeed> response) {
                        if (response.isSuccess()) {
                            mBus.post(new PicasaEvent.OnLoaded(response.body()));
                        } else {
                            int statusCode = response.code();
                            ResponseBody errorBody = response.errorBody();
                            try {
                                mBus.post(new PicasaEvent.OnLoadingError(errorBody.string(), statusCode));
                            } catch (IOException e) {
                                mBus.post(PicasaEvent.FAILED);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PicasaFeed> call, Throwable error) {
                        if (error != null && error.getMessage() != null) {
                            mBus.post(new PicasaEvent.OnLoadingError(error.getMessage(), -1));
                        } else {
                            mBus.post(PicasaEvent.FAILED);
                        }
                    }
                });
    }

    @Subscribe
    public void onUploadingStart(PicasaEvent.OnUploadingStart onLoadingStart) {
        PicasaAlbum album = new PicasaAlbum();
        album.setTitle("My New Album2");

        mPicasaClient.getPicasaService().createAlbum(onLoadingStart.getUsername(), album).enqueue(new Callback<PicasaAlbum>() {
            @Override
            public void onResponse(Call<PicasaAlbum> call, Response<PicasaAlbum> response) {
                if (response.isSuccess()) {
                    Log.d("picasa", "onResponse: isSuccess");
                } else {
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    try {
                        mBus.post(new PicasaEvent.OnLoadingError(errorBody.string(), statusCode));
                        Log.d("picasa", "onResponse: " + errorBody.string() + statusCode);
                    } catch (IOException e) {
                        Log.d("picasa", "onResponse: failed");
                        mBus.post(PicasaEvent.FAILED);
                    }
                }
            }

            @Override
            public void onFailure(Call<PicasaAlbum> call, Throwable t) {
                if (t != null && t.getMessage() != null) {
                    Log.d("picasa", "onFailure: " + t.getMessage());
                } else {
                    mBus.post(PicasaEvent.FAILED);
                    Log.d("picasa", "onFailure: failed");
                }
            }
        });
    }

    @Subscribe
    public void onUploadingPhotoStart(PicasaEvent.OnUploadingPhotoStart onLoadingStart) {

        final MediaType IMAGE = MediaType.parse("image/jpeg");

        Uri uriPath = Uri.parse("file:/storage/sdcard0/Pictures/Traveler's Diary/тест/ТЕСТ_20160223_1301301980047786.jpg");
        String path = uriPath.getPath();
        File file = new File(path);

        RequestBody requestBody = RequestBody.create(IMAGE, file);

        mPicasaClient.getPicasaService().uploadPhoto(onLoadingStart.getUsername(),
                onLoadingStart.getAlbumId(), requestBody).enqueue(new Callback<PicasaPhoto>() {
            @Override
            public void onResponse(Call<PicasaPhoto> call, Response<PicasaPhoto> response) {
                if (response.isSuccess()) {
                    Log.d("picasa", "onResponse: isSuccess");
                } else {
                    int statusCode = response.code();
                    ResponseBody errorBody = response.errorBody();
                    try {
                        mBus.post(new PicasaEvent.OnLoadingError(errorBody.string(), statusCode));
                        Log.d("picasa", "onResponse: " + errorBody.string() + statusCode);
                    } catch (IOException e) {
                        Log.d("picasa", "onResponse: failed");
                        mBus.post(PicasaEvent.FAILED);
                    }
                }
            }

            @Override
            public void onFailure(Call<PicasaPhoto> call, Throwable t) {
                if (t != null && t.getMessage() != null) {
                    Log.d("picasa", "onFailure: " + t.getMessage());
                } else {
                    mBus.post(PicasaEvent.FAILED);
                    Log.d("picasa", "onFailure: failed");
                }
            }
        });
    }

}