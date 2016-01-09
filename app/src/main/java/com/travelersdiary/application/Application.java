package com.travelersdiary.application;

import com.firebase.client.Firebase;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        // enable disk persistence
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
    }
}
