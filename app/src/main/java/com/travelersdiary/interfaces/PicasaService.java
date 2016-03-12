package com.travelersdiary.interfaces;

import com.travelersdiary.models.picasa.PicasaAlbum;
import com.travelersdiary.models.picasa.PicasaFeed;
import com.travelersdiary.models.picasa.PicasaPhoto;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PicasaService {

    @GET("feed/api/user/{username}")
    Call<PicasaFeed> feed(@Path("username") String username);

    @Headers("Content-Type: application/atom+xml")
    @POST("feed/api/user/{username}")
    Call<PicasaAlbum> createAlbum(@Path("username") String username, @Body PicasaAlbum album);

    @POST("feed/api/user/{username}/albumid/{albumId}")
    Call<PicasaPhoto> uploadPhoto(@Path("username") String username,
                                  @Path("albumId") String albumId,
                                  @Body RequestBody file);

}
