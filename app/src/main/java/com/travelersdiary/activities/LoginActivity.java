package com.travelersdiary.activities;

import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.ui.auth.google.GoogleAuthProvider;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener {

    @Bind(R.id.sign_in_button)
    SignInButton mGoogleLoginButton;

    private static final int RC_GOOGLE_LOGIN = 65001;

    private Firebase mFirebaseRef;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private ProgressDialog progressDialog;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleLoginButton.setSize(SignInButton.SIZE_WIDE);

        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_LOGIN && resultCode == RESULT_OK) {
            String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    .requestScopes(new Scope(Scopes.PLUS_ME))
                    .requestScopes(new Scope("https://picasaweb.google.com/data/"))
                    .setAccountName(email)
                    .build();

            GoogleApiClient gac = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(gac);

            if (pendingResult.isDone()) {
                handleGoogleSignInResult(pendingResult.get());
            } else {
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        handleGoogleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @OnClick(R.id.sign_in_button)
    public void googleSignIn() {
        startActivityForResult(AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null),
                RC_GOOGLE_LOGIN);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.login_activity_error_dialog_title))
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        progressDialog.dismiss();

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount acc = result.getSignInAccount();
            if (acc != null) {
                mSharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_ID, acc.getId()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_DISPLAY_NAME, acc.getDisplayName()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_EMAIL, acc.getEmail()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_PROFILE_IMAGE, acc.getPhotoUrl().toString()).apply();
                mSharedPreferences.edit().putString(Constants.KEY_USER_GOOGLE_TOKEN, acc.getIdToken()).apply();

                firebaseAuthWithGoogle(acc);
            }
        } else {
            // Google Sign In failed, update UI appropriately
            Log.d("Login", "Google Sign In failed!");
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acc) {
        Log.d("Login", "firebaseAuthWithGoogle:" + acc.getId());

        getCoverImage(acc.getId());

        AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Login", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            if (user != null) {
                                String id = "google:" + acc.getId();

                                mSharedPreferences.edit().putString(Constants.KEY_PROVIDER, user.getProviderId()).apply();
                                mSharedPreferences.edit().putString(Constants.KEY_USER_UID, id).apply();

                                Map<String, Object> map = new HashMap<>();
                                map.put(Constants.FIREBASE_USER_NAME, acc.getDisplayName());
                                map.put(Constants.FIREBASE_USER_EMAIL, acc.getEmail());
                                new Firebase(Utils.getFirebaseUserUrl(id)).updateChildren(map);

                                /* Go to main activity */
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
    }

    public void getCoverImage(String id) {
        String coverEndpoint = "https://www.googleapis.com/plus/v1/people/" + id
                + "?fields=cover%2FcoverPhoto%2Furl&key=" + Constants.GOOGLE_API_SERVER_KEY;

        Request request = new Request.Builder()
                .url(coverEndpoint)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String coverImage = jsonObject.getJSONObject("cover").getJSONObject("coverPhoto").getString("url");

                        mSharedPreferences.edit().putString(Constants.KEY_COVER_IMAGE, coverImage).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("LoginActivity", "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        auth.removeAuthStateListener(this);
        super.onStop();
    }
}
