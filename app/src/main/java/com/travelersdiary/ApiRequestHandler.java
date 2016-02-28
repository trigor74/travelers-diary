package com.travelersdiary;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.travelersdiary.events.LoadPicasaAlbumsEvent;
import com.travelersdiary.picasa_model.PicasaFeed;

import java.io.IOException;

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
    public void onLoadingStart(LoadPicasaAlbumsEvent.OnLoadingStart onLoadingStart) {
        mPicasaClient.getPicasaService().feed(onLoadingStart.getRequest())
                .enqueue(new Callback<PicasaFeed>() {

                    @Override
                    public void onResponse(Call<PicasaFeed> call, Response<PicasaFeed> response) {
                        if (response.isSuccess()) {
                            mBus.post(new LoadPicasaAlbumsEvent.OnLoaded(response.body()));
                        } else {
                            int statusCode = response.code();
                            ResponseBody errorBody = response.errorBody();
                            try {
                                mBus.post(new LoadPicasaAlbumsEvent.OnLoadingError(errorBody.string(), statusCode));
                            } catch (IOException e) {
                                mBus.post(LoadPicasaAlbumsEvent.FAILED);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<PicasaFeed> call, Throwable error) {
                        if (error != null && error.getMessage() != null) {
                            mBus.post(new LoadPicasaAlbumsEvent.OnLoadingError(error.getMessage(), -1));
                        } else {
                            mBus.post(LoadPicasaAlbumsEvent.FAILED);
                        }
                    }
                });
    }

}