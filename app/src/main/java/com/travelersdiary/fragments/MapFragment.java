package com.travelersdiary.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.travelersdiary.Constants;
import com.travelersdiary.R;
import com.travelersdiary.Utils;
import com.travelersdiary.activities.DiaryActivity;
import com.travelersdiary.activities.ReminderItemActivity;
import com.travelersdiary.models.DiaryNote;
import com.travelersdiary.models.LocationPoint;
import com.travelersdiary.models.ReminderItem;
import com.travelersdiary.models.TrackListItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapLongClickListener, OnInfoWindowClickListener {

    private String mTravelId = null;

    private GoogleMap mMap;
    private MapView mMapView;

    private LatLngBounds.Builder mLatLngBoundsBuilder = new LatLngBounds.Builder();
    private boolean mHasLatLngBoundsBuilderPoints = false;
    private Map<Marker, String> mNoteRefsMap = new HashMap<>();
    private Map<Marker, String> mTodoRefsMap = new HashMap<>();
    private Map<Marker, Circle> mTodoCirclesMap = new HashMap<>();
    private Map<Polyline, Marker> mRouteStartMarksMap = new HashMap<>();
    private Map<Polyline, Marker> mRouteEndMarksMap = new HashMap<>();

    private ArrayList<LatLng> mRoutePoints;
    private Marker myMarker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);

        mTravelId = getActivity().getIntent().getStringExtra(Constants.KEY_TRAVEL_REF);

        mMapView = (MapView) view.findViewById(R.id.map_container);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        if (mTravelId != null && !mTravelId.isEmpty()) {
            retrieveDataAndShowOnMap();
        }
        //mMap.setMyLocationEnabled(true);
    }

    // TODO: 20.03.16 remove examples

    /**
     * example of drawing track lines
     */
    private void drawTrack() {
        mRoutePoints = new ArrayList<>();

        LatLng mapPoint1 = new LatLng(15.127, 16.123);
        LatLng mapPoint2 = new LatLng(16.127, 17.123);
        LatLng mapPoint3 = new LatLng(23.645, 19.23);
        LatLng mapPoint4 = new LatLng(53.127, 43.123);

        mRoutePoints.add(mapPoint1);
        mRoutePoints.add(mapPoint2);
        mRoutePoints.add(mapPoint3);
        mRoutePoints.add(mapPoint4);

        Polyline route = mMap.addPolyline(new PolylineOptions()
                .width(5f)
                .color(getResources().getColor(R.color.colorPrimaryDark))
                .geodesic(true)
                .zIndex(2f));
        route.setPoints(mRoutePoints);
    }

    /**
     * example of adding markers
     */
    private void addMarkers() {
        mMap.addMarker(new MarkerOptions()
                .title("Start")
                .snippet("27 Dec 10:30")
                .position(mRoutePoints.get(0)));

        mMap.addMarker(new MarkerOptions()
                .title("End")
                .snippet("27 Dec 15:40")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .position(mRoutePoints.get(mRoutePoints.size() - 1)));


        mMap.addMarker(new MarkerOptions()
                .title("Custom marker")
                .snippet("move me")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(new LatLng(30.543, 33.154)));
    }

    /**
     * adding marker on long click
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        myMarker = mMap.addMarker(new MarkerOptions()
                .title("My Marker")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng));
    }

    /**
     * handling on marker info window click
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.equals(myMarker)) {
            Toast.makeText(getContext(), "Info window clicked", Toast.LENGTH_SHORT).show();
            myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        if (mNoteRefsMap.containsKey(marker)) {
            String key = mNoteRefsMap.get(marker);
            Intent intent = new Intent(getContext(), DiaryActivity.class);
            intent.putExtra(Constants.KEY_DAIRY_NOTE_REF, key);
            getContext().startActivity(intent);
        }

        if (mTodoRefsMap.containsKey(marker)) {
            String key = mTodoRefsMap.get(marker);
            Intent intent = new Intent(getContext(), ReminderItemActivity.class);
            intent.putExtra(Constants.KEY_REMINDER_ITEM_REF, key);
            getContext().startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        mMapView.onDestroy();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void retrieveDataAndShowOnMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String userUID = sharedPreferences.getString(Constants.KEY_USER_UID, null);

        Firebase tracks = new Firebase(Utils.getFirebaseUserTracksUrl(userUID));
        Query query = tracks.orderByChild(Constants.FIREBASE_REMINDER_TRAVELID).equalTo(mTravelId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    TrackListItem track = child.getValue(TrackListItem.class);
                    //child.getKey();

                    TreeMap<Long, LocationPoint> sortedTrack = new TreeMap<>(track.getTrack());
                    if (!sortedTrack.isEmpty()) {
                        Long firstTime = sortedTrack.firstEntry().getKey();
                        LatLng firstPoint = sortedTrack.firstEntry().getValue().getLatLng();
                        Long lastTime = sortedTrack.lastEntry().getKey();
                        LatLng lastPoint = sortedTrack.lastEntry().getValue().getLatLng();

                        List<LatLng> trackPoints = new ArrayList<>(sortedTrack.size());
                        for (Map.Entry<Long, LocationPoint> entry : sortedTrack.entrySet()) {
                            LatLng latLng = entry.getValue().getLatLng();
                            trackPoints.add(latLng);
                            mLatLngBoundsBuilder.include(latLng);
                            mHasLatLngBoundsBuilderPoints = true;
                        }

                        Polyline route = mMap.addPolyline(new PolylineOptions()
                                .width(5f)
                                .color(ContextCompat.getColor(getContext(), R.color.mapRouteColor))
                                .geodesic(true)
                                .zIndex(2f));
                        route.setPoints(trackPoints);

                        Marker startMarker = mMap.addMarker(new MarkerOptions()
                                .title("Start")
                                .snippet(Utils.getMediumDate(firstTime))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .position(firstPoint));

                        Marker endMarker = mMap.addMarker(new MarkerOptions()
                                .title("End")
                                .snippet(Utils.getMediumDate(lastTime))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .position(lastPoint));

                        //store start/end markers of the track
                        mRouteStartMarksMap.put(route, startMarker);
                        mRouteEndMarksMap.put(route, endMarker);
                    }
                }

                if (mHasLatLngBoundsBuilderPoints) {
                    LatLngBounds latLngBounds = mLatLngBoundsBuilder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 16));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        Firebase userDiaryRef = new Firebase(Utils.getFirebaseUserDiaryUrl(userUID));
        Query diaryQuery = userDiaryRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(mTravelId);
        diaryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    DiaryNote note = child.getValue(DiaryNote.class);
                    if (note.getLocation() != null) {
                        Marker noteMarker = mMap.addMarker(new MarkerOptions()
                                .title(note.getTitle())
                                .snippet(Utils.getMediumDate(note.getTime()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .position(note.getLocation().getLatLng()));
                        mLatLngBoundsBuilder.include(note.getLocation().getLatLng());
                        mHasLatLngBoundsBuilderPoints = true;

                        //save reference key to map
                        mNoteRefsMap.put(noteMarker, child.getKey());
                    }
                }

                if (mHasLatLngBoundsBuilderPoints) {
                    LatLngBounds latLngBounds = mLatLngBoundsBuilder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 16));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        Firebase reminderRef = new Firebase(Utils.getFirebaseUserReminderUrl(userUID));
        Query reminderQuery = reminderRef.orderByChild(Constants.FIREBASE_DIARY_TRAVELID).equalTo(mTravelId);
        reminderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ReminderItem item = child.getValue(ReminderItem.class);
                    if (Constants.FIREBASE_REMINDER_TASK_ITEM_TYPE_LOCATION.equals(item.getType()) &&
                            item.getWaypoint().getLocation() != null) {
                        String title = item.getTitle();
                        if (title == null || title.isEmpty()) {
                            title = getResources().getString(R.string.reminder_no_title_text);
                        }
                        Marker todoMarker = mMap.addMarker(new MarkerOptions()
                                .title(title)
                                .snippet(item.getWaypoint().getTitle())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                .position(item.getWaypoint().getLocation().getLatLng()));
                        Circle todoCircle = mMap.addCircle(new CircleOptions()
                                .center(item.getWaypoint().getLocation().getLatLng())
                                .radius(item.getDistance())
                                .fillColor(ContextCompat.getColor(getContext(), R.color.mapCircleFillColor))
                                .strokeColor(ContextCompat.getColor(getContext(), R.color.mapCircleStrokeColor))
                                .strokeWidth(2));
                        mLatLngBoundsBuilder.include(item.getWaypoint().getLocation().getLatLng());
                        mHasLatLngBoundsBuilderPoints = true;

                        //store reminder mark refs and circle
                        mTodoRefsMap.put(todoMarker, child.getKey());
                        mTodoCirclesMap.put(todoMarker, todoCircle);
                    }
                }

                if (mHasLatLngBoundsBuilderPoints) {
                    LatLngBounds latLngBounds = mLatLngBoundsBuilder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 16));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}