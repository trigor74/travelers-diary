package com.travelersdiary.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

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

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.sign_in_button)
    SignInButton mGoogleLoginButton;

    private static final int RC_GOOGLE_LOGIN = 65001;

    private final OkHttpClient mClient = new OkHttpClient();

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mGoogleConnectionResult;

    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;

    private Firebase mFirebaseRef;
    private Firebase.AuthStateListener mAuthStateListener;

    private ProgressDialog mAuthProgressDialog;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        mGoogleLoginButton.setSize(SignInButton.SIZE_WIDE);

        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.login_activity_progress_dialog_title));
        mAuthProgressDialog.setMessage(getString(R.string.login_activity_progress_dialog_message));
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();

        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.dismiss();
                /**
                 * If there is a valid session to be restored, start MainActivity.
                 * No need to pass data via SharedPreferences because app
                 * already holds userName/provider data from the latest session
                 */
                if (authData != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseRef.addAuthStateListener(mAuthStateListener);
    }

    @OnClick(R.id.sign_in_button)
    public void googleSignIn() {
        mGoogleLoginClicked = true;
        if (!mGoogleApiClient.isConnecting()) {
            if (mGoogleConnectionResult != null) {
                resolveSignInError();
            } else if (mGoogleApiClient.isConnected()) {
                getGoogleOAuthTokenAndLogin();
            } else {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Show errors to users
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.login_activity_error_dialog_title))
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
            showErrorDialog(firebaseError.getMessage());
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
        AsyncTask<Void, Void, HashMap<String, String>> task = new AsyncTask<Void, Void, HashMap<String, String>>() {
            String errorMessage = null;

            @Override
            protected HashMap<String, String> doInBackground(Void... params) {
                String token = null;
                String id = null;
                String coverUrl = null;

                HashMap<String, String> result = new HashMap<>();

                try {
                    String scope = String.format("oauth2:email %s", Scopes.PLUS_LOGIN);
                    id = GoogleAuthUtil.getAccountId(LoginActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient));
                    token = GoogleAuthUtil.getToken(LoginActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    errorMessage = getString(R.string.login_activity_error_message_network, transientEx.getMessage());
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
                    errorMessage = getString(R.string.login_activity_error_message_auth, authEx.getMessage());
                }

                try {
                    coverUrl = getCoverImageUrl(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result.put("token", token);
                result.put("coverUrl", coverUrl);

                return result;
            }

            @Override
            protected void onPostExecute(HashMap<String, String> result) {
                mGoogleLoginClicked = false;
                String token = result.get("token");
                String coverUrl = result.get("coverUrl");

                if (coverUrl != null) {
                    mSharedPreferences.edit().putString(Constants.KEY_COVER_IMAGE, coverUrl).apply();
                }

                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    mFirebaseRef.authWithOAuthToken(Constants.GOOGLE_PROVIDER, token, new AuthResultHandler());
                } else if (errorMessage != null) {
                    mAuthProgressDialog.hide();
                    showErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }

    public String getCoverImageUrl(String id) throws Exception {
        String coverJsonUrl = "https://www.googleapis.com/plus/v1/people/" + id +
                "?fields=cover%2FcoverPhoto%2Furl&key=" + Constants.GOOGLE_API_SERVER_KEY;
        Request request = new Request.Builder()
                .url(coverJsonUrl)
                .build();

        Response response = mClient.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        String jsonData = response.body().string();
        JSONObject jsonObject = new JSONObject(jsonData);

        return jsonObject.getJSONObject("cover").getJSONObject("coverPhoto").getString("url");
    }

    @Override
    public void onConnected(final Bundle bundle) {
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
    protected void onPause() {
        mFirebaseRef.removeAuthStateListener(mAuthStateListener);
        super.onPause();
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            /* Hide all the login buttons */
            //mGoogleLoginButton.setVisibility(View.GONE);
            /* If user has logged in with Google provider */
            if (authData.getProvider().equals(Constants.GOOGLE_PROVIDER)) {
                mSharedPreferences.edit().putString(Constants.KEY_PROVIDER, authData.getProvider()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_USER_UID, authData.getUid()).apply();

                String name = (String) authData.getProviderData().get(Constants.GOOGLE_DISPLAY_NAME);
                mSharedPreferences.edit().putString(Constants.KEY_DISPLAY_NAME, name).apply();

                String email = (String) authData.getProviderData().get(Constants.GOOGLE_EMAIL);
                mSharedPreferences.edit().putString(Constants.KEY_EMAIL, email).apply();

                String profileImageURL = (String) authData.getProviderData().get(Constants.GOOGLE_PROFILE_IMAGE);
                mSharedPreferences.edit().putString(Constants.KEY_PROFILE_IMAGE, profileImageURL).apply();
            } else {
                showErrorDialog(getString(R.string.login_activity_error_message_invalid_provider, authData.getProvider()));
            }
        } else {
            /* No authenticated user show all the login buttons */
            //mGoogleLoginButton.setVisibility(View.VISIBLE);
        }
        /* Go to main activity */
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
