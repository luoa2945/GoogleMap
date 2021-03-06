package com.example.amy.googlemaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.stats.StatsEvent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private Location myLocation;
    private static final float MY_LOC_ZOOM_FACTOR = 20f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MyMapsApp", "onCreate: map has been created");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sanfran = new LatLng(37.77, -122.43);
        mMap.addMarker(new MarkerOptions().position(sanfran).title("Born here"));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission check 1");
            Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        //mMap.setMyLocationEnabled(true);
    }

    public void trackMe(View v) {
        if (!(isGPSenabled || isNetworkEnabled)) {
            Log.d("MyMapsApp", "trackMe: calling getLocation");
            getLocation(v);
            Log.d("MyMapsApp", "trackMe: called getLocation");
        } else {
            isGPSenabled = isNetworkEnabled = false;
            locationManager.removeUpdates(locationListenerGps);
            Log.d("MyMapsApp", "trackMe: removed GPS updates");
            locationManager.removeUpdates(locationListenerNetwork);
            Log.d("MyMapsApp", "trackMe: removed Network updates");
            Toast.makeText(getApplicationContext(), "No longer tracking", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //mMap.setMyLocationEnabled(true);
            Log.d("MyMapsApp", "trackMe: No longer tracking.");
        }
    }

    public void getLocation(View v) {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMapsApp", "getLocation: GPS is enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation: Network is enabled");

            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: No Provider is enabled");
            } else {
                mMap.setMyLocationEnabled(false);

                this.canGetLocation = true;
                if (isGPSenabled) {
                    Log.d("MyMapsApp", "getLocation: GPS Enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("MyMapsApp", "Failed Permission check 1");
                        Log.d("MyMapsApp", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                    }
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                        Log.d("MyMapsApp", "Failed Permission check 2");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    Log.d("MyMapsApp", "getLocation: GPS update request success");
                    Toast.makeText(getApplicationContext(), "Using GPS", Toast.LENGTH_SHORT).show();
                }
                if (isNetworkEnabled) {
                    Log.d("MyMapsApp", "getLocation: Network Enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);
                    Log.d("MyMapsApp", "getLocation: Network update request success");
                    Toast.makeText(getApplicationContext(), "Using Network", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught an exeption in getLocation");
            e.printStackTrace();
        }
    }

    public void setView(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

    public void dropAmarker(String provider) {
        LatLng userLocation;
        if (locationManager != null) {
            Log.d("MyMapsApp","dropAMarker: locationManager != null");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMapsApp","dropAMarker: permissions failed");
                return;
            }
            myLocation = new Location(provider);
        }

        if (myLocation == null) {
            //display a message in Log.d and/or Toast
            Log.d("MyMapsApp", "dropAMarker: Location is null");
            Toast.makeText(getApplicationContext(), "myLocation is invalid",Toast.LENGTH_SHORT).show();
        } else {
            //Add a shape for your marker
            if (provider.equals("Network")) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.RED)
                        .fillColor(Color.RED));
                Log.d("MyMapsApp", "dropAMarker: Network Marker added!");
            } else {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1.5)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.BLUE));
                Log.d("MyMapsApp", "dropAMarker: GPS Marker added!");
            }

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            mMap.animateCamera(update);
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            Log.d("MyMapsApp", "locationListenerGPS: GPS location has changed.");
            Toast.makeText(getApplicationContext(), "GPS location has changed. ", Toast.LENGTH_SHORT).show();
            //drop a marker on the map(create a method called dropAmarker)
            dropAmarker("GPS");
            //disable network updates (see LocationManager API to remove updates) if gps is available
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "onLocationChanged: GPS Status changed");
            Toast.makeText(getApplicationContext(), "GPS Status changed", Toast.LENGTH_SHORT).show();
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "onLocationChanged: GPS Status changed");
                    Toast.makeText(getApplicationContext(), "GPS Status changed", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp", "onLocationChanged: GPS Status changed");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMapsApp", "onLocationChanged: GPS Status changed");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
                    break;
                default:
                    //request updates from NETWORK_PROVIDER
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                            .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGps);
            }
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output message in Log.d and Toast
            //drop a marker on the map(create a method called dropAmarker)
            //relaunch request for network location updates (requestLocationUpdates(NETWORK_PROVIDER)
            Log.d("MyMapsApp", "onLocationChanged: Network location changed");
            Toast.makeText(getApplicationContext(), "Network location changed", Toast.LENGTH_SHORT).show();
            dropAmarker("Network");
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerGps);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in Log.d and/or Toast
            Log.d("MyMapsApp", "onStatusChanged: Network status changed");
            Toast.makeText(getApplicationContext(), "Network status changed", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String proFvider) {}
    };

    public void onSearch(View v)
    {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mMap.clear();
        Log.d("MyMapsApp", "onSearch: map has been cleared");

        EditText location_text = (EditText)findViewById(R.id.Search_Text);
        String location = location_text.getText().toString();
        List<Address> addressList = null;
        Toast.makeText(getApplicationContext(), "Location entered", Toast.LENGTH_SHORT).show();
        if (! location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation == null)
            {
                Log.d ("MyMapsApp", "GPS loc null, now network");
                myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            try {
                Log.d("MyMapsApp", "onSearch: getting addresses");
                Log.d("MyMapsApp", "onSearch: my location" + myLocation.getLongitude());
                Toast.makeText(getApplicationContext(), "getting addresses", Toast.LENGTH_SHORT).show();
                addressList = geocoder.getFromLocationName(location, 5, myLocation.getLatitude()-0.03246,
                        myLocation.getLongitude() - 0.03332387845, myLocation.getLatitude() + 0.0324637681,
                        myLocation.getLongitude() + 0.03332387845);
                //addressList = geocoder.getFromLocationName(location, 200);
                Log.d("MyMapsApp", "onSearch: address list generated");
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int i = 0; i < addressList.size(); i++) {
                Address address = addressList.get(i);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                /*if (Math.abs(address.getLatitude() - myLocation.getLatitude()) <= (5 * 0.01666) && Math.abs(address.getLatitude() - myLocation.getLatitude()) <= 5 * 0.01666) {
                    {
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    }
                }*/

            }
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 10.0f);

            mMap.animateCamera(update);
            Log.d("MyMapsApp", "onSearch: Places found");
        }
        else
        {
            Log.d("MyMapsApp", "getSearch: empty Search Text");
            Toast.makeText(getApplicationContext(), "No location entered", Toast.LENGTH_SHORT).show();
        }
    }

    public void clearMarkers(View v)
    {
        Log.d("MyMapsApp", "clearMarkers: clearing Markers");
        mMap.clear();
        Toast.makeText(getApplicationContext(), "Markers cleared", Toast.LENGTH_SHORT).show();
    }

}