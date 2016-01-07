package com.travelersdiary.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
import com.travelersdiary.R;

import java.io.IOException;

import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

        mGoogleLoginButton = (SignInButton) findViewById(R.id.sign_in_button);

        mGoogleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        mGoogleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

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

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
                    mFirebaseRef.authWithOAuthToken("google", token, new Firebase.AuthResultHandler() {
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
                    });
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
            /* Store the ConnectionResult so that we can use it later
            when the user clicks on the Google+ login button */
            mGoogleConnectionResult = result;

            if (mGoogleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors
                 until the user is signed in, or they cancel. */
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
                Uri profileImageURL = (Uri) authData.getProviderData().get("profileImageURL");
            } else {
                // Invalid provider
            }
        } else {
            /* No authenticated user show all the login buttons */
            mGoogleLoginButton.setVisibility(View.VISIBLE);
        }
        this.mAuthData = authData;
    }
}