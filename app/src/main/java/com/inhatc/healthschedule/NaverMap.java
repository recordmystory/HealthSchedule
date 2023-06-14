package com.inhatc.healthschedule;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

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
import com.naver.maps.map.overlay.CircleOverlay;
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
    Button btnArrived;

    private com.naver.maps.map.NaverMap mMap;
    private LatLng Start_location = null;
    private LatLng Current_location;
    private LatLng Arrived_location;
    private String strPosition = null;
    private int intStart = 0;

    double arrivedLatitude = 0;
    double arrivedLongitude = 0;
    LocationManager locationManager;
    LocationListener locationListener;

    double totalDistance = 0;
    private TextView totalDistanceTextView;

    long startTime;  //초기 시작시간
    long beforeTime;  //직전 측정시간
    long currentTime; //현재 측정시간

    double totalSpeed;  //전체속도
    double currentSpeed; //현재속도

    String exerciseGubun = null;
    double exerciseTimeSecond = 0;
    double totalCostCalorie = 0;
    double costCaloriePerSecond = 0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naver_map);

        btnArrived = findViewById(R.id.btnArrived);
        btnArrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ScheduleListActivity.class);
                intent.putExtra("totalCostCalorie", (int)totalCostCalorie);
                intent.putExtra("exerciseTimeSecond", (int)exerciseTimeSecond);
                intent.putExtra("totalDistance", (int)totalDistance);
                setResult(RESULT_OK, intent);   //layout 종료하면서 값 전달
                finish();
            }
        });

        totalDistanceTextView = (TextView) findViewById(R.id.totalDistanceTextView);
        //현재위치 찾기 추가
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

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
        //현재위치 찾기 추가
        mMap.getUiSettings().setLocationButtonEnabled(true);
        mMap.setLocationSource(locationSource);

        long minTime = 10000;   //10초마다 (단위 milliSecond)
        float minDistance = 20; //20M 마다

        mMap.setMapType(com.naver.maps.map.NaverMap.MapType.Basic);
        mMap.setSymbolScale(1.0f);

        //지도가 준비 되고 난 후 도착지점 표시
        Intent intent = getIntent();
        arrivedLatitude = intent.getDoubleExtra("arrivedLatitude", 0);
        arrivedLongitude = intent.getDoubleExtra("arrivedLongitude", 0);
        exerciseGubun = intent.getStringExtra("exerciseGubun");

        Arrived_location = new LatLng(arrivedLatitude, arrivedLongitude);
        strPosition = "도착 지점";
        mDisplayMarker(Arrived_location, "#0000FF"); //파랑

        if ("걷기".equals(exerciseGubun)) {
            costCaloriePerSecond = 2.4 / 60.0;
        } else if ("달리기".equals(exerciseGubun)) {
            costCaloriePerSecond = 8.0 / 60.0;
        } else if ("자전거".equals(exerciseGubun)) {
            costCaloriePerSecond = 3.7 / 60.0;
        }



        CircleOverlay circle = new CircleOverlay();
        circle.setCenter(new LatLng(arrivedLatitude, arrivedLongitude));
        circle.setRadius(55);  //m단위
        circle.setColor(Color.parseColor("#CCFFFF"));
        circle.setMap(naverMap);

        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Arrived_location);
        mMap.moveCamera(cameraUpdate);

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
        Toast.makeText(this, "Location Services has been changed to " + provider, Toast.LENGTH_LONG).show();
    }

    public void mUpdateMap(Location location) {

        double dLatitude = location.getLatitude();
        double dLongitude = location.getLongitude();

        Current_location = new LatLng(dLatitude, dLongitude);

        //처음 출발 위치
        if (Start_location == null) {
            LocationOverlay locationOverlay = mMap.getLocationOverlay();
            locationOverlay.setVisible(true);
            locationOverlay.setPosition(Current_location);

            strPosition = "출발";
            mDisplayMarker(Current_location, "#0000FF");    //파랑
            intStart ++;
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Current_location);
            mMap.moveCamera(cameraUpdate);

            startTime = System.currentTimeMillis();  //초기 시작시간
            beforeTime = startTime;

        } else {
            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(Current_location);
            mMap.moveCamera(cameraUpdate);

            PathOverlay path = new PathOverlay();
            path.setCoords(Arrays.asList(
                    new LatLng(Start_location.latitude, Start_location.longitude),
                    new LatLng(Current_location.latitude, Current_location.longitude)
            ));
            path.setMap(mMap);
            path.setOutlineColor(Color.GRAY);
            path.setColor(Color.BLUE);
            path.setWidth(10);

            //두 위치간의 거리 계산
            Location startLocation = new Location("startLocation");
            startLocation.setLatitude(Start_location.latitude);
            startLocation.setLongitude(Start_location.longitude);

            Location currentLocation = new Location("currentLocation");
            startLocation.setLatitude(Current_location.latitude);
            startLocation.setLongitude(Current_location.longitude);

            double distance = getDistance(Start_location.latitude, Start_location.longitude, Current_location.latitude, Current_location.longitude);
            totalDistance += distance;  //총 이동거리(M)
            strPosition = String.valueOf(intStart);

            intStart ++;

            //현재 속도 계산
            currentTime = System.currentTimeMillis();
            currentSpeed = distance * 1000.00 / (currentTime - beforeTime);
            //전체 속도 계산
            totalSpeed = totalDistance * 1000.00 / (currentTime - startTime);
            totalCostCalorie = (currentTime - startTime) / 1000.0 * costCaloriePerSecond;
            totalDistanceTextView.setText("운동 종류 : " + exerciseGubun + "\n");
            totalDistanceTextView.append("총 이동거리 : " + String.format("%.1f", totalDistance) + " m\n");
            totalDistanceTextView.append("현재 이동거리 : " + String.format("%.1f", distance) + " m\n");
            totalDistanceTextView.append("전체속도 : " + String.format("%.1f", totalSpeed) + " m/s\n");
            totalDistanceTextView.append("현재속도 : " + String.format("%.1f", currentSpeed) + " m/s\n");
            totalDistanceTextView.append("시간차 : " + (currentTime - beforeTime) + " milsecond\n");
            totalDistanceTextView.append("소요 칼로리 : " + String.format("%.1f", totalCostCalorie) + " kcal\n");

            //totalDistanceTextView.append("위도 차이 : " + Math.abs(arrivedLatitude - Current_location.latitude) + " \n");
            //totalDistanceTextView.append("경도 차이 : " + Math.abs(arrivedLongitude - Current_location.longitude) + " \n");

            //반경 50m 안에 있는 지 확인, 위도경도상으로만 하면 정사각형 모양
            //0.0001 = 1.1M
            //0.009 = 99M
            //0.0045 = 50M
            // +- 편차니깐 0.00225 = 25M로 계산해야한다?
            if (Math.abs(arrivedLatitude - Current_location.latitude) <= 0.0005 && Math.abs(arrivedLongitude - Current_location.longitude) <= 0.0005) {
                btnArrived.setEnabled(true);
                mDisplayMarker(Current_location, "#0000FF");    //파랑
                Toast.makeText(getApplicationContext(), "도착하였습니다.", Toast.LENGTH_SHORT).show();
                totalDistanceTextView.append("도착하였습니다.\n");

            } else {
                mDisplayMarker(Current_location, "#00FF00");    //그린
            }

            startLocation.setLatitude(Current_location.latitude);
            startLocation.setLongitude(Current_location.longitude);
            beforeTime = currentTime;
            exerciseTimeSecond = (currentTime - startTime) / 1000;
        }

        Start_location = Current_location;
    }

    private void mDisplayMarker(LatLng objLocation, String color) {
        Marker objMK = new Marker();
        objMK.setVisible(false);
        objMK.setPosition(objLocation);
        objMK.setIconTintColor(Color.parseColor(color));
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


    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6372.8 * 1000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(sin(dLat / 2), 2) + Math.pow(sin(dLon / 2), 2) * cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2));
        double c = 2 * asin(sqrt(a));
        return (R * c);
        //return (R * c).toInt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) locationManager.removeUpdates(locationListener);
    }

    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        // 기존의 뒤로가기 버튼의 기능 제거
        // super.onBackPressed();

        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2초 이내에 뒤로가기 버튼을 한번 더 클릭시 finish()(앱 종료)
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }



}