package com.travelersdiary.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.BindingAdapter;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
import com.travelersdiary.BaseViewModel;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.travel.list.TravelListActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;


public class LoginViewModel extends BaseViewModel implements GoogleApiClient.OnConnectionFailedListener {

    private class AuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            view.hideAuthProgressDialog();
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            view.hideAuthProgressDialog();
            view.showErrorDialog(firebaseError.getMessage());
        }
    }

    private static final int RC_GOOGLE_LOGIN = 65001;

    private Firebase.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    private Firebase firebaseRef;

    private Context context;
    private LoginView view;

    private SharedPreferences sharedPreferences;

    public LoginViewModel(LoginView view) {
        this.view = view;
    }

    public void onSignInClick() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        view.startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
    }

    public void init(AppCompatActivity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestScopes(new Scope("https://picasaweb.google.com/data/"))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        authStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                view.hideAuthProgressDialog();

                /**
                 * If there is a valid session to be restored, start TravelListActivity.
                 * No need to pass data via SharedPreferences because app
                 * already holds userName/provider data from the latest session
                 */
                if (authData != null) {
                    startMainActivity();
                }
            }
        };
    }

    @Override
    public void start(Context context) {
        this.context = context;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        firebaseRef = new Firebase(Constants.FIREBASE_URL);

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            /**
             * If the user's cached credentials are valid, the OptionalPendingResult will be "done"
             * and the GoogleSignInResult will be available instantly.
             */
            GoogleSignInResult result = opr.get();
            handleGoogleSignInResult(result);
        } else {
            /**
             * If the user has not previously signed in on this device or the sign-in has expired,
             * this asynchronous branch will attempt to sign in the user silently.  Cross-device
             * single sign-on will occur in this branch.
             */
            view.showAuthProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleGoogleSignInResult(googleSignInResult);
                }
            });
        }

        firebaseRef.addAuthStateListener(authStateListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /** An unresolvable error has occurred and Google APIs (including Sign-In) will not be available. */
        Timber.d("onConnectionFailed: %s", connectionResult);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            /** Signed in successfully */
            GoogleSignInAccount googleSignInAccount = result.getSignInAccount();
            String googleId = googleSignInAccount.getId();
            String name = googleSignInAccount.getDisplayName();
            String email = googleSignInAccount.getEmail();
            String profileImageURL = googleSignInAccount.getPhotoUrl() != null
                    ? googleSignInAccount.getPhotoUrl().toString() : null;

            sharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_ID, googleId).apply();
            sharedPreferences.edit().putString(Constants.KEY_DISPLAY_NAME, name).apply();
            sharedPreferences.edit().putString(Constants.KEY_EMAIL, email).apply();
            sharedPreferences.edit().putString(Constants.KEY_PROFILE_IMAGE, profileImageURL).apply();

            getGoogleOAuthTokenAndLogin(email, googleId);
        }
    }

    private void getGoogleOAuthTokenAndLogin(final String email, final String googleId) {
        view.showAuthProgressDialog();

        /**
         * Request the token with the minimal scopes
         * Get User`s G+ Cover Image Url
         */
        AsyncTask<Void, Void, HashMap<String, String>> task = new AsyncTask<Void, Void, HashMap<String, String>>() {
            String errorMessage = null;

            @Override
            protected HashMap<String, String> doInBackground(Void... params) {
                String token = null;
                String coverUrl = null;

                HashMap<String, String> result = new HashMap<>();

                try {
                    String scopes = "oauth2:profile email";
                    token = GoogleAuthUtil.getToken(context.getApplicationContext(), email, scopes);
                } catch (IOException transientEx) {
                    /** Network or server error */
                    errorMessage = context.getString(R.string.login_activity_error_message_network, transientEx.getMessage());
                } catch (GoogleAuthException authEx) {
                    /**
                     * The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed.
                     */
                    errorMessage = context.getString(R.string.login_activity_error_message_auth, authEx.getMessage());
                }

                try {
                    coverUrl = getCoverImageUrl(googleId);
                } catch (Exception e) {
                    Timber.e(e, e.getLocalizedMessage());
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
                    sharedPreferences.edit().putString(Constants.KEY_COVER_IMAGE, coverUrl).apply();
                }

                if (token != null) {
                    /** Successfully got OAuth token, now login with Google */
                    sharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_TOKEN, token).apply();
                    firebaseRef.authWithOAuthToken(Constants.GOOGLE_PROVIDER, token, new AuthResultHandler());
                } else if (errorMessage != null) {
                    view.hideAuthProgressDialog();
                    view.showErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }

    private String getCoverImageUrl(String id) throws Exception {
        String coverJsonUrl = "https://www.googleapis.com/plus/v1/people/"
                + id + "?fields=cover%2FcoverPhoto%2Furl&key=" + Constants.GOOGLE_API_SERVER_KEY;
        Request request = new Request.Builder()
                .url(coverJsonUrl)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String jsonData = response.body().string();
        JSONObject jsonObject = new JSONObject(jsonData);

        return jsonObject.getJSONObject("cover").getJSONObject("coverPhoto").getString("url");
    }

    /**
     * Once a user is logged in, take the mAuthData provided from Firebase and "use" it.
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            if (authData.getProvider().equals(Constants.GOOGLE_PROVIDER)) {
                sharedPreferences.edit().putString(Constants.KEY_PROVIDER, authData.getProvider()).apply();
                sharedPreferences.edit().putString(Constants.KEY_USER_UID, authData.getUid()).apply();
            } else {
                view.showErrorDialog(context.getString(R.string.login_activity_error_message_invalid_provider, authData.getProvider()));
            }

            Map<String, Object> map = new HashMap<>();
            map.put(Constants.FIREBASE_USER_NAME, sharedPreferences.getString(Constants.KEY_DISPLAY_NAME, null));
            map.put(Constants.FIREBASE_USER_EMAIL, sharedPreferences.getString(Constants.KEY_EMAIL, null));
            new Firebase(Utils.getFirebaseUserUrl(authData.getUid())).updateChildren(map);

            startMainActivity();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(context, TravelListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        view.startActivityAndFinish(intent);
    }

    @Override
    public void stop() {
        this.context = null;
        firebaseRef.removeAuthStateListener(authStateListener);
    }

    @BindingAdapter({"signIn"})
    public static void onSignInClick(SignInButton button, View.OnClickListener listener) {
        button.setOnClickListener(listener);
    }

}
