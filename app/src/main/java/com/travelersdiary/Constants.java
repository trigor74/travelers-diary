package com.travelersdiary;

/**
 * Constants class store most important strings and paths of the app
 */

public final class Constants {
    // firebase
    public static final String FIREBASE_URL = BuildConfig.FIREBASE_ROOT_URL;

    // google
    public static final String GOOGLE_API_KEY = BuildConfig.GOOGLE_API_KEY;
    public static final String GOOGLE_ANDROID_CLIENT_ID = BuildConfig.GOOGLE_ANDROID_CLIENT_ID;
    public static final String GOOGLE_PROVIDER = "google";
    public static final String GOOGLE_EMAIL = "email";
    public static final String GOOGLE_DISPLAY_NAME = "displayName";
    public static final String GOOGLE_PROFILE_IMAGE = "profileImageURL";

    // shared preferences
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_USER_UID = "USER_UID";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_DISPLAY_NAME = "DISPLAY_NAME";
    public static final String KEY_PROFILE_IMAGE = "PROFILE_IMAGE";
}
