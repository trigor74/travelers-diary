package com.travelersdiary;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.firebase.client.Firebase;
import com.onegravity.rteditor.fonts.FontManager;

import io.fabric.sdk.android.Fabric;

public class App extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //pre-load fonts for rtEditor
        FontManager.preLoadFonts(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
