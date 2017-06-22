package com.travelersdiary;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.travelersdiary.base.BaseActivity;
import com.travelersdiary.models.Photo;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.services.AlarmSetterService;
import com.travelersdiary.services.LocationTrackingService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Helper class with methods
 */

public class Utils {
    public static String getFirebaseUserUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID;
    }

    public static String getFirebaseUserActiveTravelUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_ACTIVE_TRAVEL;
    }

    public static String getFirebaseUserTravelsUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRAVELS;
    }

    public static String getFirebaseUserDiaryUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_DIARY;
    }

    public static String getFirebaseUserTracksUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_TRACKS;
    }

    public static String getFirebaseUserReminderUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_REMINDER;
    }

    public static String getFirebaseUserWaypointsUrl(String userUID) {
        return Constants.FIREBASE_URL + "/" + Constants.FIREBASE_USERS + "/" + userUID + "/" + Constants.FIREBASE_WAYPOINTS;
    }

    public static void setStatusBarColor(Context context, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppCompatActivity activity = (AppCompatActivity) context;
            if (activity != null) {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(color));
            }
        }
    }

    public static void clearImageCache(final Context context) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Glide.get(context).clearDiskCache();
                return true;
            }
        };
        task.execute();
    }

    public static void tintWidget(Context context, View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, color));
        view.setBackground(wrappedDrawable);
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        Cursor cursor = null;
        try {
            String filePath = imageFile.getAbsolutePath();
            cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID},
                    MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                } else {
                    return null;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean checkFileExists(Context context, String uri) {
        if (uri == null) {
            return false;
        }

        Uri uriPath = Uri.parse(uri);
        String path = Utils.getRealPathFromURI(context, uriPath);

        if (path != null) {
            File file = new File(path);
            return file.exists();
        }

        return false;
    }

    public static ArrayList<String> photoArrayToStringArray(Context context, ArrayList<Photo> images) {

        ArrayList<String> albumImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            if (checkFileExists(context, images.get(i).getLocalUri())) {
                albumImages.add(images.get(i).getLocalUri());
            } else {
                albumImages.add(images.get(i).getPicasaUri());
            }
        }

        return albumImages;
    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo networkInfo = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else if (!networkInfo.isConnected() || networkInfo.isRoaming()) {
            return false;
        }
        return true;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getMediumDate(long timestamp) {
        String date = SimpleDateFormat.getDateInstance().format(timestamp);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp);
        return date + " " + time;

    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public static boolean isTabletLandMode(Context context) {
        return context.getResources().getBoolean(R.bool.isTabletLand);
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public static void startTravel(Context context, String travelKey, String travelTitle) {
        /*
          For switch active travel logic see listener mActiveTravelListener in BaseActivity class
        */

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);
        String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
        if (activeTravelKey != null && !Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(activeTravelKey)) {
            setStopTime(context, activeTravelKey);
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.FIREBASE_ACTIVE_TRAVEL_TITLE, travelTitle);
        map.put(Constants.FIREBASE_ACTIVE_TRAVEL_KEY, travelKey);
        Firebase activeTravelRef = new Firebase(Utils.getFirebaseUserActiveTravelUrl(userUID));
        activeTravelRef.setValue(map);

        ((BaseActivity) context).enableStartTrackingButton(true);
    }

    public static void stopTravel(Context context, String travelKey) {
        if (!Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY.equals(travelKey)) {
            setStopTime(context, travelKey);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String activeTravelKey = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
            if (activeTravelKey != null && activeTravelKey.equals(travelKey)) {
                startTravel(context, Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY, context.getString(R.string.default_travel_title));
            }
            ((BaseActivity) context).stopTracking();
            ((BaseActivity) context).enableStartTrackingButton(false);
        }
    }

    public static void setStopTime(Context context, String travelKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(Constants.FIREBASE_TRAVEL_STOP_TIME, System.currentTimeMillis());
        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                .child(travelKey)
                .updateChildren(map);
    }

    public static void deleteTravel(final Context context, final String travelKey) {
        new AlertDialog.Builder(context)
                .setInverseBackgroundForced(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(context.getString(R.string.travels_delete_question_text))
                .setMessage(context.getString(R.string.travels_delete_warning_text))
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String activeTravel = sharedPreferences.getString(Constants.KEY_ACTIVE_TRAVEL_KEY, null);
                        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

                        // delete reminder items
                        Query query = new Firebase(Utils.getFirebaseUserReminderUrl(userUID))
                                .orderByChild(Constants.FIREBASE_REMINDER_TRAVELID)
                                .equalTo(travelKey);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    // remove notifications for current active travel
                                    Utils.disableAlarmGeofence(
                                            context.getApplicationContext(),
                                            child.getKey());

                                    child.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                            }
                        });

                        if (activeTravel != null && activeTravel.equals(travelKey)) {
                            // stop tracking
                            Intent intentStopTracking = new Intent(context, LocationTrackingService.class);
                            intentStopTracking.setAction(LocationTrackingService.ACTION_STOP_TRACK);
                            context.startService(intentStopTracking);

                            sharedPreferences.edit()
                                    .putString(Constants.KEY_ACTIVE_TRAVEL_KEY, null)
                                    .apply();
                            sharedPreferences.edit()
                                    .putString(Constants.KEY_ACTIVE_TRAVEL_TITLE, null)
                                    .apply();
                            Utils.startTravel(context, Constants.FIREBASE_TRAVELS_DEFAULT_TRAVEL_KEY,
                                    context.getString(R.string.default_travel_title));
                        }
                        // delete tracks
                        new Firebase(Utils.getFirebaseUserTracksUrl(userUID))
                                .child(travelKey)
                                .removeValue();
                        // delete travel
                        new Firebase(Utils.getFirebaseUserTravelsUrl(userUID))
                                .child(travelKey)
                                .removeValue();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    public static void disableAlarmGeofence(Context context, String itemKey) {
        AlarmSetterService.cancelAlarm(context, itemKey);
        new GeofenceSetter(context).cancelGeofence(itemKey);
    }

    public static void enableAlarmGeofence(Context context, ReminderItem reminderItem, String itemKey) {
        if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_TIME.equals(reminderItem.getType())) {
            AlarmSetterService.setAlarm(context, reminderItem, itemKey);
        } else if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(reminderItem.getType())) {
            new GeofenceSetter(context).setGeofence(reminderItem, itemKey);
        }
    }
}
