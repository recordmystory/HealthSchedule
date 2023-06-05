package com.inhatc.healthschedule;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NaverMap extends FragmentActivity implements OnMapReadyCallback {

    MapFragment mapFragment;

    private com.naver.maps.map.NaverMap mMap;
    private LatLng Start_location = null;
    private LatLng Current_location;
    private LatLng Center_location;
    private String strPosition = null;
    private String strCaption = null;
    private int intStart = 0;

    LocationManager locationManager;
    LocationListener locationListener;

    //정중앙 마커 위칭값
    double arrivedLatitude = 0;
    double arrivedLongitude = 0;

    private TextView arrivedAddressTextView;
    String arrivedAddress = null;
    
    //
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naver_map);

        //api 데이터 화면표시
        arrivedAddressTextView = (TextView) findViewById(R.id.arrivedAddress);

        //현재위치 찾기 추가
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        //이 위치를 도착지점으로 설정
        Button btnArrived = (Button) findViewById(R.id.btnArrived);
        btnArrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.putExtra("arrivedLatitude", arrivedLatitude); //id 전달
                intent.putExtra("arrivedLongitude", arrivedLongitude); //회원번호 전달
                intent.putExtra("arrivedAddress", arrivedAddress); //주소 전달
                setResult(RESULT_OK, intent);   //layout 종료하면서 값 전달
                finish();
                //Intent intent = new Intent(getApplicationContext(), NfcReader.class);
                //startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull com.naver.maps.map.NaverMap naverMap) {

        mMap = naverMap;

        //현재위치 찾기 추가
        mMap.getUiSettings().setLocationButtonEnabled(true);
        mMap.setLocationSource(locationSource);

        CameraPosition cameraPosition = mMap.getCameraPosition();



        //마커 정중앙에 표시
        double dLatitude = cameraPosition.target.latitude;
        double dLongitude = cameraPosition .target.longitude;
        Center_location = new LatLng(dLatitude, dLongitude);

        Marker centerMarker = new Marker();
        centerMarker.setPosition(Center_location);
        centerMarker.setMap(mMap);
        centerMarker.setVisible(true);
        // 카메라의 움직임에 대한 이벤트 리스너 인터페이스.
        mMap.addOnCameraChangeListener((reason, animated) -> {
            System.out.println("NaverMap 카메라 변경 - reson: " + reason + ", animated: " + animated);
            Center_location = new LatLng(mMap.getCameraPosition().target.latitude, naverMap.getCameraPosition().target.longitude);
            centerMarker.setPosition(Center_location);
        });

        // 카메라의 움직임 종료에 대한 이벤트 리스너 인터페이스.
        mMap.addOnCameraIdleListener(() -> {
            Toast.makeText(getApplicationContext(), "카메라 움직임 종료", Toast.LENGTH_SHORT).show();
            arrivedLatitude = mMap.getCameraPosition().target.latitude;
            arrivedLongitude = mMap.getCameraPosition().target.longitude;
            arrivedAddress = getAddress(arrivedLatitude,arrivedLongitude);

            arrivedAddressTextView.setText(arrivedAddress);

            System.out.println("정중앙 위치값");
            System.out.println("x : " + arrivedLatitude);
            System.out.println("y : " + arrivedLongitude);
        });

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
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

            LocationOverlay locationOverlay = mMap.getLocationOverlay();
            locationOverlay.setVisible(true);
            locationOverlay.setPosition(Current_location);

            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Current_location);
            mMap.moveCamera(cameraUpdate);

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

        //이건 마커 텍스트 보여줄 때
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


    //
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                mMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    public String getAddress(double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mapFragment.getContext(), Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            //Toast.makeText(baseContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();
            nowAddress  = "주소를 가져 올 수 없습니다.";
            e.printStackTrace();

        }
        return nowAddress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

}