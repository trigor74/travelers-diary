package com.travelersdiary.application;

import com.firebase.client.Firebase;
import com.onegravity.rteditor.fonts.FontManager;
import com.squareup.otto.Bus;
import com.travelersdiary.ApiRequestHandler;
import com.travelersdiary.BusProvider;

public class Application extends android.app.Application {
    public static ApiRequestHandler mApiRequestHandler;
    private Bus mBus = BusProvider.bus();

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //pre-load fonts for rtEditor
        FontManager.preLoadFonts(this);

//        mApiRequestHandler = new ApiRequestHandler(mBus);
//        mBus.register(mApiRequestHandler);
    }

}
