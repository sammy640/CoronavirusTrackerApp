package com.example.coronavirustracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import android.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener {
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003 ;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private DatabaseReference myLocationRef;
    private GeoFire geofire;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private static final String TAG = "MainActivity";
    private static final String TAGG = "cool";
    private Location lastLocation;
    private double currLatitude;
    private double currLongitude;
    private LocationResult LocationResult;
    private LocationRequest locationRequest;
    private com.google.android.gms.location.LocationCallback locationCallback;
    private String userKey;
    private String userNode;
    private ArrayList<LatLng> locationList;
    private TileProvider mProvider;
    private TileOverlay mOverlay;
    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(checkMapServices()){
            if(mLocationPermissionGranted){
                getLocationPermission();
            }
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(!mLocationPermissionGranted) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        // Getting reference to database
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference("Danger Location");

        userKey = getAlphaNumericString(15);
        userNode = getAlphaNumericString(13);

        // creates Geofire object around user's current location
        settingGeoFire();
    }

    private void settingGeoFire() {
        Log.d(TAGG, "cmmmm 90");
        myLocationRef = FirebaseDatabase.getInstance().getReference(userNode);
        geofire = new GeoFire(myLocationRef);
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
        Log.d(TAG, "onmapreadycalled");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // gets current location
        fusedLocationClient = getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(20 * 1000);
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                LocationResult = locationResult;currLatitude = locationResult.getLastLocation().getLatitude();
                currLongitude = locationResult.getLastLocation().getLongitude();

                // sets the location user's geofire to current location
                geofire.setLocation(userNode, new GeoLocation(currLatitude,
                        currLongitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            Log.d(TAG, "There was an error saving the location to GeoFire: " + error);
                        } else {
                            Log.d(TAG, "Location saved on server successfully!");
                            Log.d(userKey, "Location saved on server successfully!");
                        }
                    }

                });

                if (locationResult == null) {
                    return;
                } else {

                    /*if (fusedLocationClient != null) {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                    }*/
                }
            }
        },
                Looper.myLooper());

        // Adding cluster manager
        mClusterManager = new ClusterManager<MyItem>(this, mMap);
        mClusterManager.setAnimation(false);
        MyItem marker = new MyItem(0.0, 0.0, "Test", "Test");
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        // Adds all the cases of Coronavirus as markers on map
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                    locationList = new ArrayList<>();
                    for (DataSnapshot ds2 : ds1.getChildren()) {
                        double lat, lon;
                        String confirmed = ds2.child("Confirmed").getValue().toString();
                        Double confirmedNum = Double.parseDouble(confirmed);
                        float hue;
                        lat = Double.parseDouble(ds2.child("Latitude").getValue().toString());
                        lon = Double.parseDouble(ds2.child("Longitude").getValue().toString());
                        LatLng loc = new LatLng(lat, lon);
                    /*String description = "Country/Region: " +
                            ds2.child("Country").getValue().toString();
                    String snip = "State/Province: " + ds2.child("Province").getValue().toString()
                            + "\n" + "Confirmed cases" + confirmed;

                 //setting color of marker based on number of confirmed cases
                 if(confirmedNum >= 10 && confirmedNum < 100) {
                        hue = 60f; // yellow
                    } else if(confirmedNum >= 100 && confirmedNum < 1000) {
                        hue = 30f; //orange
                    } else if(confirmedNum >= 1000 && confirmedNum < 10000) {
                        hue = 0f; // red
                    } else if(confirmedNum >= 10000) {
                        hue = 270f; // violet
                    } else {
                    hue = 120f; // green
                }

                    mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title(description)
                            .snippet(snip)
                            .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                            */
                    /* mMap.addCircle(new CircleOptions()
                            .center(loc)
                            .radius(100)
                            .strokeColor(Color.BLACK)
                            .fillColor(Color.RED));

                     // Adds all cases as clusters
*/                      MyItem marker = new MyItem(lat, lon);
                        mClusterManager.addItem(marker);

                        locationList.add(new LatLng(lat, lon));

                        // creates a Geoquery around each case location with 1km radius
                        GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(lat, lon), 1f);
                        Log.d(TAG, currLatitude + "");
                        geoQuery.addGeoQueryEventListener(MapsActivity.this);
                    }
                    int[] colors = {

                            Color.rgb(255, 0, 0),   // red
                            Color.rgb(0, 96, 255), //blue
                            Color.rgb(28,0,128), // purple

                    };

                    float[] startPoints = {
                            0.8f, 0.9f ,1f
                    };

                    Gradient gradient = new Gradient(colors, startPoints);

                    mProvider = new HeatmapTileProvider.Builder()
                            .data(locationList)
                            .radius(20)
                            .opacity(1.0)
                            .gradient(gradient)
                            .build();
                    // Add a tile overlay to the map, using the heat map tile provider.
                    mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                    mOverlay.setVisible(true);
                }
                // Create a heat map tile provider, passing it the latlngs

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLatitude, currLongitude), 0.5f));

            }
            @Override
            public void onCancelled (@NonNull DatabaseError databaseError){

            }
        });
    }

    // creates notification based on user's location with respect to virus's locations
    private void sendNotification(String title, String format) {
        String NOTIFICATION_CHANNEL_ID = "coronavirus_tracker";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("channel description");
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(format)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(false);
        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Log.d(TAG, "Complete entered area");
        sendNotification("Coronavirus Tracker", "You have entered a Coronavirus zone");
    }

    @Override
    public void onKeyExited(String key) {
        Log.d(TAG, "Complete exited area");
        sendNotification("Coronavirus Tracker", String.format("You have exited a Coronavirus zone", key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("Coronavirus Tracker", String.format("You have moved within a Coronavirus zone", key));

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        Toast.makeText(this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onresumecalled");
        super.onResume();

    }

    public static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }

    private void addHeatMap() {

    }

}
