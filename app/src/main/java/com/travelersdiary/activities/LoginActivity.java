package com.travelersdiary.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
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
        GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.sign_in_button)
    SignInButton mGoogleLoginButton;

    private static final int RC_GOOGLE_LOGIN = 65001;

    private final OkHttpClient mClient = new OkHttpClient();

    private GoogleApiClient mGoogleApiClient;

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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestScopes(new Scope("https://picasaweb.google.com/data/"))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
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
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleGoogleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            mAuthProgressDialog.show();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleGoogleSignInResult(googleSignInResult);
                }
            });
        }

        mFirebaseRef.addAuthStateListener(mAuthStateListener);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully

            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            String googleId = googleSignInAccount.getId();
            String name = googleSignInAccount.getDisplayName();
            String email = googleSignInAccount.getEmail();
            String profileImageURL = googleSignInAccount.getPhotoUrl() != null
                    ? googleSignInAccount.getPhotoUrl().toString() : null;

            mSharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_ID, googleId).apply();
            mSharedPreferences.edit().putString(Constants.KEY_DISPLAY_NAME, name).apply();
            mSharedPreferences.edit().putString(Constants.KEY_EMAIL, email).apply();
            mSharedPreferences.edit().putString(Constants.KEY_PROFILE_IMAGE, profileImageURL).apply();

            getGoogleOAuthTokenAndLogin(email, googleId);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }


    @OnClick(R.id.sign_in_button)
    public void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
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

    private void getGoogleOAuthTokenAndLogin(final String email, final String googleId) {
        mAuthProgressDialog.show();
        /* Request the token with the minimal scopes */
        /* Get User`s G+ Cover Image Url */

        AsyncTask<Void, Void, HashMap<String, String>> task = new AsyncTask<Void, Void, HashMap<String, String>>() {
            String errorMessage = null;

            @Override
            protected HashMap<String, String> doInBackground(Void... params) {
                String token = null;
                String coverUrl = null;

                HashMap<String, String> result = new HashMap<>();

                try {
                    String scopes = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(getApplicationContext(), email, scopes);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    errorMessage = getString(R.string.login_activity_error_message_network, transientEx.getMessage());
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    errorMessage = getString(R.string.login_activity_error_message_auth, authEx.getMessage());
                }

                try {
                    coverUrl = getCoverImageUrl(googleId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result.put("token", token);
                result.put("coverUrl", coverUrl);

                return result;
            }

            @Override
            protected void onPostExecute(HashMap<String, String> result) {
                String token = result.get("token");
                String coverUrl = result.get("coverUrl");

                if (coverUrl != null) {
                    mSharedPreferences.edit().putString(Constants.KEY_COVER_IMAGE, coverUrl).apply();
                }

                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    mSharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_TOKEN, token).apply();
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
        String coverJsonUrl = "https://www.googleapis.com/plus/v1/people/"
                + id + "?fields=cover%2FcoverPhoto%2Furl&key=" + Constants.GOOGLE_API_SERVER_KEY;
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("LoginActivity", "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        mFirebaseRef.removeAuthStateListener(mAuthStateListener);
        super.onStop();
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            if (authData.getProvider().equals(Constants.GOOGLE_PROVIDER)) {
                mSharedPreferences.edit().putString(Constants.KEY_PROVIDER, authData.getProvider()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_USER_UID, authData.getUid()).apply();
            } else {
                showErrorDialog(getString(R.string.login_activity_error_message_invalid_provider, authData.getProvider()));
            }
            /* Go to main activity */
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
