package com.travelersdiary.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.travelersdiary.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private Firebase myFirebaseRef;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1;
    private String idToken;
    private String userDisplayName;
    private String userEmail;
    private Uri userPhoto;

    @Bind(R.id.btn_login)
    Button btnLogin;
    @Bind(R.id.sign_in_button)
    SignInButton signInButton;

    @OnClick(R.id.btn_login)
    public void onClick(View v) {
        myFirebaseRef.authWithOAuthToken("google", idToken, new Firebase.AuthResultHandler() {

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

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("835719391495-he70fi2ddfap2t8ecv1uvke5vsqf1c19.apps.googleusercontent.com")
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                idToken = acct.getIdToken();
                userDisplayName = acct.getDisplayName();
                userEmail = acct.getEmail();
                userPhoto = acct.getPhotoUrl();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(getApplicationContext(), "An unresolvable error has occurred!", Toast.LENGTH_SHORT).show();
    }
}
