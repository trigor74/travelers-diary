package com.travelersdiary.application;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.onegravity.rteditor.fonts.FontManager;
import io.fabric.sdk.android.Fabric;
import com.squareup.otto.Bus;
import com.travelersdiary.ApiRequestHandler;
import com.travelersdiary.BusProvider;

public class Application extends android.app.Application {
    public static ApiRequestHandler mApiRequestHandler;
    private Bus mBus = BusProvider.bus();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //pre-load fonts for rtEditor
        FontManager.preLoadFonts(this);

//        mApiRequestHandler = new ApiRequestHandler(mBus);
//        mBus.register(mApiRequestHandler);
    }

}
