package com.travelersdiary;

/**
 * Constants class store most important strings and paths of the app
 */

public final class Constants {
    // firebase
    public static final String FIREBASE_URL = BuildConfig.FIREBASE_ROOT_URL;
    public static final String FIREBASE_USERS = "users";
    public static final String FIREBASE_DIARY = "diary";
    public static final String FIREBASE_TRAVELS = "travels";
    public static final String FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY = "default";
    public static final String FIREBASE_TRAVEL_TITLE = "title";
    public static final String FIREBASE_TRAVEL_DESCRIPTION = "description";
    public static final String FIREBASE_TRACKS = "tracks";
    public static final String FIREBASE_REMINDER = "reminder";
    public static final String FIREBASE_REMINDER_TRAVELID = "travelId";
    public static final String FIREBASE_REMINDER_ACTIVE = "active";
    public static final String FIREBASE_REMINDER_TASK = "task";
    public static final String FIREBASE_REMINDER_TASK_ITEM = "item";
    public static final String FIREBASE_REMINDER_TASK_ITEM_CHECKED = "checked";
    public static final String FIREBASE_REMINDER_TASK_ITEM_TYPE = "type";
    public static final String FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME = "time";
    public static final String FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION = "location";
    public static final String FIREBASE_WAYPOINTS = "waypoints";
    public static final String FIREBASE_DIARY_TRAVELID = "travelId";
    public static final String FIREBASE_DIARY_TRAVEL_TITLE = "travelTitle";
    public static final String FIREBASE_DIARY_TIME = "time";
    public static final String FIREBASE_DIARY_PHOTOS = "photos";

    // google
    public static final String GOOGLE_API_KEY = BuildConfig.GOOGLE_API_KEY;
    public static final String GOOGLE_API_SERVER_KEY = BuildConfig.GOOGLE_API_SERVER_KEY;
    public static final String GOOGLE_ANDROID_CLIENT_ID = BuildConfig.GOOGLE_ANDROID_CLIENT_ID;
    public static final String GOOGLE_PROVIDER = "google";
    public static final String GOOGLE_ID = "id";
    public static final String GOOGLE_EMAIL = "email";
    public static final String GOOGLE_DISPLAY_NAME = "displayName";
    public static final String GOOGLE_PROFILE_IMAGE = "profileImageURL";

    // shared preferences
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_USER_UID = "USER_UID";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_DISPLAY_NAME = "DISPLAY_NAME";
    public static final String KEY_PROFILE_IMAGE = "PROFILE_IMAGE";
    public static final String KEY_COVER_IMAGE = "COVER_IMAGE";

    // travel
    public static final String KEY_TRAVEL_REF = "TRAVEL_REF";
    public static final String KEY_TRAVEL_KEY = "TRAVEL_KEY";
    public static final String KEY_TRAVEL_TITLE = "TRAVEL_TITLE";
    public static final String KEY_TRAVEL_DESCRIPTION = "TRAVEL_DESCRIPTION";

    // dairy
    public static final String KEY_DAIRY_NOTE_REF = "DAIRY_NOTE_REF";

    // reminder
    public static final String KEY_REMINDER_ITEM_REF = "REMINDER_ITEM_REF";

    public static final int PHOTO_SPAN_COUNT = 3;

    // request codes
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int GALLERY_REQUEST_CODE = 21;
    public static final int ENTER_ALBUM_REQUEST_CODE = 22;
}
