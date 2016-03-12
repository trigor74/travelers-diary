package com.travelersdiary;

import com.travelersdiary.interfaces.PicasaService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class PicasaClient {

    public static final String API_ROOT = "https://picasaweb.google.com/data/";

    private static volatile PicasaClient instance;

    private PicasaService mPicasaService;

    public static PicasaClient getInstance() {
        PicasaClient localInstance = instance;
        if (localInstance == null) {
            synchronized (PicasaClient.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new PicasaClient();
                }
            }
        }
        return localInstance;
    }

    public void createService(final String authToken) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(interceptor);

        if (authToken != null) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/atom+xml")
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_ROOT)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(client)
                .build();

        mPicasaService = retrofit.create(PicasaService.class);
    }

    public PicasaService getPicasaService() {
        return mPicasaService;
    }

}
