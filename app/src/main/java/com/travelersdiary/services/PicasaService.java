package com.travelersdiary.services;

import com.travelersdiary.picasa_model.PicasaAlbum;
import com.travelersdiary.picasa_model.PicasaFeed;
import com.travelersdiary.picasa_model.PicasaPhoto;

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


//    @Multipart
//    @POST("feed/api/user/{username}/albumid/6257415133015328049")
//    Call<PicasaAlbum> uploadPhoto(@Path("username") String username,
//                                  @Headers("Content-Type: multipart/related"),
////                                  @Path("albumid") String albumid,
////                                  @Part("metadata") RequestBody metadata,
//                                  @Part("image") RequestBody file);


    @POST("feed/api/user/{username}/albumid/{albumId}")
    Call<PicasaPhoto> uploadPhoto(@Path("username") String username,
                                  @Path("albumId") String albumId,
                                  @Body RequestBody file);

}
