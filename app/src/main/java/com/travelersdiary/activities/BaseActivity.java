package com.travelersdiary.activities;

import android.net.Uri;
import android.os.Bundle;

import com.firebase.client.AuthData;

public class BaseActivity extends GoogleOAuthActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            String name = (String) authData.getProviderData().get("displayName");
            String email = (String) authData.getProviderData().get("email");
            Uri profileImageURL = Uri.parse((String) authData.getProviderData().get("profileImageURL"));
            String UUID = authData.getUid();
        }
    }
}
