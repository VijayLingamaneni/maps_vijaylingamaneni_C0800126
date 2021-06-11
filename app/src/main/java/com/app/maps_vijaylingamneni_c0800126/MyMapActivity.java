package com.app.maps_vijaylingamneni_c0800126;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LocationRequest mLocationRequest;
    private final int REQUEST_CHECK_SETTINGS = 2;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private List<Marker> markerList = new ArrayList();
    private PolygonOptions mPolygonOptions;
    private Polygon mPolygon;
    private LatLng currentLatLng;
    List<Polyline> polylineList = new ArrayList();
    Marker polylineMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initCurrentLocationConfig();

    }

    private void initCurrentLocationConfig(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocation(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (googleMap != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15.0f));

                }
            }
        });

    }

    protected void requestLocation(final LocationListener currentLocationListener) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        stopLocationUpdates();
                        currentLocationListener.onLocationChanged(location);
                        break;

                    }

                }
            }
        };

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MyMapActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }


    private void startLocationUpdates() {

        LocationPermissionHelper.askPermission(MyMapActivity.this, LocationPermissionHelper.LOCATION, new LocationPermissionHelper.PermissionListener() {
            @Override
            public void onPermissionResult(boolean isGranted) {
                if (ActivityCompat.checkSelfPermission(MyMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if(mLocationCallback!=null)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
                Toast.makeText(MyMapActivity.this, "Location access denied.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        mPolygonOptions = new PolygonOptions();

        setPolygonClickListener();
        setMarkerClickListener();
        setPolylineClickListener();
        setMapLongClickListener();
        setMakerDragListener();
    }

    private void setMarkerClickListener(){
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                showAddress(marker.getPosition());
                return false;
            }
        });
    }

    private void setPolygonClickListener(){
        googleMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                double totalDistance = calculateDistance(markerList.get(0).getPosition(), markerList.get(1).getPosition())
                        + calculateDistance(markerList.get(1).getPosition(), markerList.get(2).getPosition())
                        + calculateDistance(markerList.get(2).getPosition(), markerList.get(3).getPosition());
                Toast.makeText(MyMapActivity.this, "Total Distance\n" + new DecimalFormat("0.##").format(totalDistance) + " Meters", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void setPolylineClickListener(){
        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                double distance = calculateDistance(polyline.getPoints().get(0),  polyline.getPoints().get(1));
                LinearLayout distanceMarkerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.polyline_view, null);

                distanceMarkerLayout.setDrawingCacheEnabled(true);
                distanceMarkerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                distanceMarkerLayout.layout(0, 0, distanceMarkerLayout.getMeasuredWidth(), distanceMarkerLayout.getMeasuredHeight());
                distanceMarkerLayout.buildDrawingCache(true);

                TextView positionDistance = (TextView) distanceMarkerLayout.findViewById(R.id.tvDistance);

                positionDistance.setText(new DecimalFormat("0.##").format(distance)+" Meters");

                Bitmap b = Bitmap.createBitmap(positionDistance.getWidth(), positionDistance.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(b);
                distanceMarkerLayout.layout(positionDistance.getLeft(), positionDistance.getTop(), positionDistance.getRight(), positionDistance.getBottom());
                distanceMarkerLayout.draw(c);
                BitmapDescriptor flagBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(b);

                double dLon = Math.toRadians(polyline.getPoints().get(1).longitude - polyline.getPoints().get(0).longitude);

                double lat1 = Math.toRadians(polyline.getPoints().get(0).latitude);
                double lat2 = Math.toRadians(polyline.getPoints().get(1).latitude);
                double lon1 = Math.toRadians(polyline.getPoints().get(0).longitude);

                double Bx = Math.cos(lat2) * Math.cos(dLon);
                double By = Math.cos(lat2) * Math.sin(dLon);
                double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
                double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

                lat3 = Math.toDegrees(lat3);
                lon3 = Math.toDegrees(lon3);

                if(polylineMarker != null){
                    polylineMarker.remove();
                }
                polylineMarker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat3, lon3))
                        .title("Distance")
                        .snippet(""+new DecimalFormat("0.##").format(distance))
                        .icon(flagBitmapDescriptor));
            }
        });
    }


    private void setMapLongClickListener() {
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                if (markerList.size() < 4) {
                    LatLng location = new LatLng(latLng.latitude, latLng.longitude);
                    int height = 160;
                    int width = 160;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.custom_marker);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    MarkerOptions markerOptions = new MarkerOptions().position(location).snippet(new DecimalFormat("0.##").format(calculateDistance(location, currentLatLng)) + " meters").title("Distance").icon(smallMarkerIcon);

                    Marker marker = googleMap.addMarker(markerOptions.draggable(true));
                    markerList.add(marker);
                    mPolygonOptions.add(latLng);
                    if (markerList.size() == 4) {
                        polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(0).getPosition(), markerList.get(1).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                        polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(1).getPosition(), markerList.get(2).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                        polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(2).getPosition(), markerList.get(3).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                        polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(3).getPosition(), markerList.get(0).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                        mPolygonOptions.strokeColor(Color.BLUE);
                        mPolygonOptions.strokeWidth(2.0f);
                        mPolygonOptions.fillColor(ContextCompat.getColor(MyMapActivity.this, R.color.green_transparent));
                        mPolygon = googleMap.addPolygon(mPolygonOptions);
                        mPolygon.setClickable(true);
                    }
                } else {
                    for (Marker m : markerList) {
                        m.remove();
                    }

                    for (Polyline polyline : polylineList) {
                        polyline.remove();
                    }
                    mPolygon.remove();
                    polylineList.clear();
                    markerList.clear();
                    if(polylineMarker !=null && polylineMarker.isVisible()){
                        polylineMarker.remove();
                    }
                    mPolygonOptions = new PolygonOptions();
                }

            }
        });
    }

    private void setMakerDragListener(){
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                if (markerList.size() == 4) {

                    for (Polyline polyline : polylineList) {
                        polyline.remove();
                    }
                    mPolygon.remove();
                    polylineList.clear();
                    polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(0).getPosition(), markerList.get(1).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                    polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(1).getPosition(), markerList.get(2).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                    polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(2).getPosition(), markerList.get(3).getPosition()).width(10).color(Color.BLUE).clickable(true)));
                    polylineList.add(googleMap.addPolyline(new PolylineOptions().add(markerList.get(3).getPosition(), markerList.get(0).getPosition()).width(10).color(Color.BLUE).clickable(true)));

                    mPolygonOptions = new PolygonOptions();
                    for (Marker m : markerList) {
                        mPolygonOptions.add(m.getPosition());
                    }
                    mPolygonOptions.strokeColor(Color.BLUE);
                    mPolygonOptions.strokeWidth(2.0f);
                    mPolygonOptions.fillColor(ContextCompat.getColor(MyMapActivity.this, R.color.green_transparent));
                    mPolygon = googleMap.addPolygon(mPolygonOptions);
                    mPolygon.setClickable(true);
                }
            }
        });
    }
    public double calculateDistance(LatLng start, LatLng end) {
        float[] distance = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, distance);
        return distance[0];

    }

    private void showAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String state = addresses.get(0).getAdminArea();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            Toast.makeText(MyMapActivity.this, knownName + ", " + state + ", " + postalCode, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}