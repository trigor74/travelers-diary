package com.travelersdiary.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.travelersdiary.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

public class MapFragment extends Fragment implements OnMapReadyCallback, OnMapLongClickListener, OnInfoWindowClickListener {

    private SupportMapFragment mFragment;
    private GoogleMap mMap;

    private ArrayList<LatLng> mRoutePoints;
    private Marker myMarker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);
        if (mFragment == null) {
            mFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_container, mFragment).commit();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        drawTrack();
        addMarkers();
    }

    /*example of drawing track lines*/
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

    /*example of adding markers*/
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

    /*adding marker on long click*/
    @Override
    public void onMapLongClick(LatLng latLng) {
        myMarker = mMap.addMarker(new MarkerOptions()
                .title("My Marker")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .position(latLng));
    }

    /*handling on marker info window click*/
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (marker.equals(myMarker)) {
            Toast.makeText(getContext(), "Info window clicked", Toast.LENGTH_SHORT).show();
            myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}