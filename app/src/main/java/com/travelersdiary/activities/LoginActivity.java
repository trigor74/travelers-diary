package com.travelersdiary.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.Travel;
import com.travelersdiary.models.UserData;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.sign_in_button)
    SignInButton mGoogleLoginButton;

    private GoogleApiClient mGoogleApiClient;
    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;
    private static final int RC_GOOGLE_LOGIN = 1;

    private Firebase mFirebaseRef;
    private ProgressDialog mAuthProgressDialog;
    private AuthData mAuthData;
    private Firebase.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        ButterKnife.bind(this);

        mGoogleLoginButton.setSize(SignInButton.SIZE_STANDARD);

        /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.hide();
                setAuthenticatedUser(authData);
            }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide any login buttons */
        mFirebaseRef.addAuthStateListener(mAuthStateListener);
    }

    @OnClick(R.id.sign_in_button)
    public void signIn() {
        mGoogleLoginClicked = true;
        if (!mGoogleApiClient.isConnecting()) {
            if (mGoogleConnectionResult != null) {
                resolveSignInError();
            } else if (mGoogleApiClient.isConnected()) {
                getGoogleOAuthTokenAndLogin();
            } else {
                    /* connect API now */
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            mAuthProgressDialog.hide();
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            mAuthProgressDialog.hide();
            showErrorDialog(firebaseError.toString());
        }
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mGoogleConnectionResult.hasResolution()) {
            try {
                mGoogleIntentInProgress = true;
                mGoogleConnectionResult.startResolutionForResult(this, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mGoogleIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        mAuthProgressDialog.show();
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(LoginActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                mGoogleLoginClicked = false;
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    mFirebaseRef.authWithOAuthToken("google", token, new AuthResultHandler());
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                    showErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        /* Connected with Google API, use this to authenticate with Firebase */
        getGoogleOAuthTokenAndLogin();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mGoogleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            mGoogleLoginClicked = false;
        }

        mGoogleIntentInProgress = false;

        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFirebaseRef.removeAuthStateListener(mAuthStateListener);
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            /* Hide all the login buttons */
            mGoogleLoginButton.setVisibility(View.GONE);
            /* show a provider specific status text */
            if (authData.getProvider().equals("google")) {
                String name = (String) authData.getProviderData().get("displayName");
                String email = (String) authData.getProviderData().get("email");
                Uri profileImageURL = Uri.parse((String) authData.getProviderData().get("profileImageURL"));
            } else {
                // Invalid provider
            }
        } else {
            /* No authenticated user show all the login buttons */
            mGoogleLoginButton.setVisibility(View.VISIBLE);
        }
        this.mAuthData = authData;
    }

    @OnClick(R.id.set_data)
    public void OnClick(View v) {
        Map<String, Travel> travels = new HashMap<String, Travel>();
        Firebase userTravelsRef = mFirebaseRef.child("users").child(mAuthData.getUid()).child("travels");
        Travel travel;
        travel = new Travel();
        travel.setTitle("Uncategorized");
        travel.setDescription("Default category");
        travel.setStart(-1);
        travel.setStop(-1);
        travel.setActive(true);
        travels.put("default", travel);
        userTravelsRef.setValue(travels);

        for (int i = 0; i < 10; i++) {
            travel = new Travel();
            travel.setTitle("Travel #" + Integer.toString(i) + " title");
            travel.setDescription("Travel #" + Integer.toString(i) + " description");
            travel.setStart(System.currentTimeMillis());
            travel.setStop(-1);
            travel.setActive(false);
            Firebase newTravelRef = userTravelsRef.push();
            newTravelRef.setValue(travel);
            travels.put(newTravelRef.getKey(), travel);
        }

        Map<String, DiaryNote> diary = new HashMap<>();

        for (Map.Entry<String, Travel> entry : travels.entrySet()) {
            String key = entry.getKey();
            //Travel value = entry.getValue();
            for (int i = 0; i < 10; i++) {
                DiaryNote note = new DiaryNote();
                note.setTravelId(key);
                note.setTitle("The #" + Integer.toString(i) + " note");
                note.setText("Text of the #" + Integer.toString(i) + " note");
                long currentTime = System.currentTimeMillis();
                note.setTime(currentTime);
                String currentTimestamp = Long.toString(currentTime);
                diary.put(currentTimestamp, note);
            }
        }

        Firebase userDiaryRef = mFirebaseRef.child("users").child(mAuthData.getUid()).child("diary");
        userDiaryRef.setValue(diary);
    }
}
