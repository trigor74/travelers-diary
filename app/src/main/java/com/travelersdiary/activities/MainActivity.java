package com.travelersdiary.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.travelersdiary.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private Firebase myFirebaseRef;

    @Bind(R.id.btn_login) Button btnLogin;
    @OnClick(R.id.btn_login) public void onClick(View v) {
        myFirebaseRef.authWithOAuthToken("google", "<oauth-token>", new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                // Authenticated successfully with payload authData
                Toast.makeText(getApplicationContext(), "Authenticated successfully with payload authData", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // Authenticated failed with error firebaseError
                Toast.makeText(getApplicationContext(), "Authenticated failed with error firebaseError", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase("https://resplendent-torch-7243.firebaseio.com/");
        myFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    // user is logged in
                    Toast.makeText(getApplicationContext(), "User is logged in!", Toast.LENGTH_SHORT).show();
                } else {
                    // user is not logged in
                    Toast.makeText(getApplicationContext(), "User is not logged in!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
