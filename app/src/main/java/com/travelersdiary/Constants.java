package com.travelersdiary;

/**
 * Constants class store most important strings and paths of the app
 */

public final class Constants {
    // firebase
    public static final String FIREBASE_URL = BuildConfig.FIREBASE_ROOT_URL;
    public static final String FIREBASE_USERS = "users";
    public static final String FIREBASE_USER_EMAIL = "email";
    public static final String FIREBASE_USER_NAME = "name";
    public static final String FIREBASE_DIARY = "diary";
    public static final String FIREBASE_ACTIVE_TRAVEL = "activeTravel";
    public static final String FIREBASE_ACTIVE_TRAVEL_KEY = "activeTravelKey";
    public static final String FIREBASE_ACTIVE_TRAVEL_TITLE = "activeTravelTitle";
    public static final String FIREBASE_TRAVELS = "travels";
    public static final String FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY = "default";
    public static final String FIREBASE_TRAVEL_TITLE = "title";
    public static final String FIREBASE_TRAVEL_DESCRIPTION = "description";
    public static final String FIREBASE_TRAVEL_DEFAULT_COVER = "defaultCover";
    public static final String FIREBASE_TRAVEL_USER_COVER = "userCover";
    public static final String FIREBASE_TRAVEL_ACTIVE = "active";
    public static final String FIREBASE_TRAVEL_START_TIME = "start";
    public static final String FIREBASE_TRAVEL_STOP_TIME = "stop";
    public static final String FIREBASE_TRAVEL_CREATION_TIME = "creationTime";
    public static final String FIREBASE_PICASA_ALABUM_ID = "picasaAlbumId";
    public static final String FIREBASE_PICASA_URI = "picasaUri";
    public static final String FIREBASE_TRACKS = "tracks";
    public static final String FIREBASE_TRACKS_TRAVELID = "travelId";
    public static final String FIREBASE_TRACKS_TRACK = "track";
    public static final String FIREBASE_REMINDER = "reminder";
    public static final String FIREBASE_REMINDER_TRAVELID = "travelId";
    public static final String FIREBASE_REMINDER_TRAVEL_TITLE = "travelTitle";
    public static final String FIREBASE_REMINDER_ACTIVE = "active";
    public static final String FIREBASE_REMINDER_COMPLETED = "completed";
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

    // openweathermap
    public static final String OPENWEATHERMAP_AIPID = BuildConfig.OPENWEATHERMAP_AIPID;

    // shared preferences
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_USER_UID = "USER_UID";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_USER_GOOGLE_TOKEN = "USER_GOOGLE_TOKEN";
    public static final String KEY_USER_GOOGLE_ID = "USER_GOOGLE_ID";
    public static final String KEY_DISPLAY_NAME = "DISPLAY_NAME";
    public static final String KEY_PROFILE_IMAGE = "PROFILE_IMAGE";
    public static final String KEY_COVER_IMAGE = "COVER_IMAGE";
    public static final String KEY_ACTIVE_TRAVEL_KEY = "ACTIVE_TRAVEL_KEY";
    public static final String KEY_ACTIVE_TRAVEL_TITLE = "ACTIVE_TRAVEL_TITLE";
    public static final String KEY_SHOW_WARNING = "SHOW_WARNING";
    public static final String KEY_IS_TRACKING_STARTED = "KEY_IS_TRACKING_STARTED";

    // travel
    public static final String KEY_TRAVEL_REF = "TRAVEL_REF";
    public static final String KEY_TRAVEL_TITLE = "TRAVEL_TITLE";
    public static final String KEY_TRAVEL_DESCRIPTION = "TRAVEL_DESCRIPTION";
    public static final String KEY_TRAVEL_DEFAULT_COVER = "TRAVEL_DEFAULT_COVER";
    public static final String KEY_TRAVEL_USER_COVER = "TRAVEL_USER_COVER";
    public static final String KEY_TRAVEL_IS_ACTIVE = "TRAVEL_IS_ACTIVE";
    public static final String KEY_TRAVEL_CREATION_TIME = "KEY_TRAVEL_CREATION_TIME";
    public static final String KEY_TRAVEL_START_TIME = "KEY_TRAVEL_START_TIME";
    public static final String KEY_TRAVEL_STOP_TIME = "KEY_TRAVEL_STOP_TIME";


    // dairy
    public static final String KEY_DAIRY_NOTE_REF = "DAIRY_NOTE_REF";

    // reminder
    public static final String KEY_REMINDER_ITEM_KEY = "REMINDER_ITEM_REF";

    public static final int PHOTO_SPAN_COUNT = 3;
    public static final int TRAVEL_DEFAULT_COVER_COUNT = 13;

    // request codes
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int GALLERY_REQUEST_CODE = 21;
    public static final int ENTER_ALBUM_REQUEST_CODE = 22;
    public static final int IMAGES_DELETE_REQUEST_CODE = 31;
    public static final int PICK_IMAGE_REQUEST_CODE = 41;

}
