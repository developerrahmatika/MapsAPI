package com.codesiana.testmaps;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView txtAddress;

    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private Marker marker;
    private PlacesClient placesClient;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        txtAddress = findViewById(R.id.txt_address);

        initPlaces();
        buildLocationRequest();
        buildLocationCallback();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this::onMapReady);

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable this location permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    private void initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000); // 15sec
        locationRequest.setFastestInterval(10000); //10sec
        locationRequest.setSmallestDisplacement(20f); //20meter
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng locationShipper = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));
                ;

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
//                        Log.d("HUBLAA", "Shipper" + locationShipper);
//                        Log.d("HUBLAA", "Center " + mMap.getProjection().getVisibleRegion().latLngBounds.getCenter());

//
//                        Single<String> singleAddress = Single.just(getAddressFromLatLng(locationResult.getLastLocation().getLatitude(),
//                                locationResult.getLastLocation().getLongitude()));


                        Single<String> singleAddress = Single.just(getAddressFromLatLng(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().latitude,
                                mMap.getProjection().getVisibleRegion().latLngBounds.getCenter().longitude));

                        Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                            @Override
                            public void onSuccess(String s) {
                                txtAddress.setText(s);
//                                marker = mMap.addMarker(new MarkerOptions()
//                                        .position(mMap.getProjection().getVisibleRegion().latLngBounds.getCenter())
//                                        .draggable(true)
//                                        .title(s));
//
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(MapsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


            }
        };
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            } else
                result = "Address not found";
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }

        return result;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("HUBLAA", "onMap");
    }


}