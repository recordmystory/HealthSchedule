package com.inhatc.healthschedule;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;

import java.util.Arrays;

public class NaverMap extends FragmentActivity implements OnMapReadyCallback {

    MapFragment mapFragment;

    private com.naver.maps.map.NaverMap mMap;
    private LatLng Start_location = null;
    private LatLng Current_location;
    private String strPosition = null;
    private String strCaption = null;
    private int intStart = 0;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naver_map);

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull com.naver.maps.map.NaverMap naverMap) {

        mMap = naverMap;
        long minTime = 10000;   //10초마다 (단위 milliSecond)
        float minDistance = 20; //20M 마다

        LatLng objLocaion;
        //double dLatitude = 37.448344;
        //double dLongitude = 126.657474;

        mMap.setMapType(com.naver.maps.map.NaverMap.MapType.Basic);
        mMap.setSymbolScale(1.0f);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mUpdateMap(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
                mAlertStatus(provider);
            }
            public void onProviderEnabled(String provider) {
                mAlertProvider(provider);
            }
            public void onProviderDisabled(String provider) {
                mCheckProvider(provider);
            }
        };

        LocationManager locationManager;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }

        String locationProvider;
        //GPS로 측정
        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, minTime, minDistance, locationListener);

        //기지국으로 측정
        //locationProvider = LocationManager.NETWORK_PROVIDER;
        //locationManager.requestLocationUpdates(locationProvider, minTime, minDistance, locationListener);

    }

    public void mCheckProvider(String provider) {
        Toast.makeText(this, provider + ": Please turn on location services...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void mAlertProvider(String provider) {
        Toast.makeText(this, provider + ": Location service is turn on!", Toast.LENGTH_LONG).show();
    }

    public void mAlertStatus(String provider) {
        Toast.makeText(this, "Location SErvices has been changed to " + provider, Toast.LENGTH_LONG).show();
    }

    public void mUpdateMap(Location location) {

        double dLatitude = location.getLatitude();
        double dLongitude = location.getLongitude();

        Current_location = new LatLng(dLatitude, dLongitude);

        if (Start_location == null) {
            CameraUpdate camerUpdate = CameraUpdate.zoomTo(15);
            mMap.moveCamera(camerUpdate);

            LocationOverlay locationOverlay = mMap.getLocationOverlay();
            locationOverlay.setVisible(true);
            locationOverlay.setPosition(Current_location);
            intStart = 0;

        } else {
            CameraUpdate cameraUpdate1 = CameraUpdate.scrollTo(Current_location);
            mMap.moveCamera(cameraUpdate1);

            PathOverlay path = new PathOverlay();
            path.setCoords(Arrays.asList(
                    new LatLng(Start_location.latitude, Start_location.longitude),
                    new LatLng(Current_location.latitude, Current_location.longitude)
            ));
            path.setMap(mMap);
            path.setOutlineColor(Color.GRAY);
            path.setColor(Color.BLUE);
            path.setWidth(10);

            if (intStart == 0) {
                strPosition = "출발";
                strCaption = "Start_location";
                mDisplayMarker(Start_location);
                intStart ++;

            } else {
                strPosition = String.valueOf(intStart);
                strCaption = String.valueOf(intStart);
                mDisplayMarker(Current_location);
                intStart ++;

            }
        }
        Start_location = Current_location;
    }

    private void mDisplayMarker(LatLng objLocation) {
        Marker objMK = new Marker();

        objMK.setVisible(false);
        objMK.setPosition(objLocation);
        objMK.setMap(mMap);
        objMK.setVisible(true);

        InfoWindow infoWindow1 = new InfoWindow();
        infoWindow1.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return strPosition;
            }
        });
        infoWindow1.open(objMK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

}