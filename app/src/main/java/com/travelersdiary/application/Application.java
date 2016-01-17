package com.travelersdiary.application;

import com.firebase.client.Firebase;
import com.onegravity.rteditor.fonts.FontManager;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //pre-load fonts for rtEditor
        FontManager.preLoadFonts(this);
    }
}
