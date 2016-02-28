package com.travelersdiary.services;

import com.travelersdiary.picasa_model.PicasaFeed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PicasaService {

    @GET("feed/api/user/{username}")
    Call<PicasaFeed> feed(@Path("username") String username);

}
