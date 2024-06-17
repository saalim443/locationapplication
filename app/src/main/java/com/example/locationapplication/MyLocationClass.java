package com.example.locationapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.location.Location;

interface MyLocationCallBack {
    public void totalDistanceGot(Double distance);
}

public class MyLocationClass extends AppCompatActivity implements MyLocationCallBack {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private List<Location> locationList = new ArrayList<>();

    private Boolean hasNotificationPermissionGranted = false;
    private Boolean hasLocationPermissionGranted = false;
    private Boolean isServiceStarted = false;

    private String API_KEY = "AIzaSyDGgHm62cLziXwWGsVPhuI68u1GpvcE-XA";
    private String BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    double totalDistance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_location_class);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rxPermission();

        Intent serviceIntent = new Intent(this, OrderTimingService.class);
        OrderTimingService.Companion.setCallBack(this);

//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopService(serviceIntent);
//                Float totalDistance = OrderTimingService.Companion.getTotalDistanceInKilometer();
//                Log.e("TAG", "Total Distance >>> " + String.format("Total Distance: %.2f kilometers", totalDistance));
//
//                TextView txtView = (TextView) findViewById(R.id.total_distance_value);
//                txtView.setText(String.format("%.2f", totalDistance));
//            }
//        });

        Button button = (Button) findViewById(R.id.start);
        Button buttonGoogle = (Button) findViewById(R.id.start_Google);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceStarted) {
                    OrderTimingService.Companion.setOpenForGoogle(false);
                    TextView txtView = (TextView) findViewById(R.id.total_distance_value);
                    txtView.setText("0.00 Km");
                    buttonGoogle.setVisibility(View.INVISIBLE);
                    if (hasLocationPermissionGranted && hasNotificationPermissionGranted) {
                        Intent intent = new Intent();
                        String packageName = getPackageName();
//                        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//                        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                            intent.setData(Uri.parse("package:" + packageName));
//                            startActivity(intent);

//                        new AlertDialog.Builder(getApplicationContext())
//                                .setTitle("Battery Optamization").
//                                setMessage("Please turn off battery optamization for better performace of this application.")
//                                .create()
//                                .show();

//                        } else {
//                            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
//                        }
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                        button.setText("Stop");
                        isServiceStarted = true;
                    } else {
//                new AlertDialog.Builder(getApplicationContext())
//                        .setTitle("Permissions Needed").
//                        setMessage("Please give all the required permissions.")
//                        .create()
//                        .show();
                    }
                }
                else {
                    stopService(serviceIntent);
                    isServiceStarted = false;
                    button.setText("Start");
                    buttonGoogle.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServiceStarted) {
                    OrderTimingService.Companion.setOpenForGoogle(true);
                    TextView txtView = (TextView) findViewById(R.id.total_distance_value);
                    txtView.setText("0.00 Km");

                    ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                    buttonGoogle.setText("Stop With Google");
                    isServiceStarted = true;
                    button.setVisibility(View.INVISIBLE);

                }
                else {
                    stopService(serviceIntent);
                    isServiceStarted = false;
                    buttonGoogle.setText("Start With Google");
                    Double totalDistance = null;
                    button.setVisibility(View.VISIBLE);
                    try {
                        new CalculateDistanceFromGoogleTask().execute(new ArrayList());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    Log.e("TAG","Locatio got on change >>> " + locationResult.getLastLocation().getLatitude());
//                    locationList.add(location);
//                    // Optionally, update the UI with the new location
//                }
//            }
//        };
//
//        startLocationUpdates();

    }

    public void notificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                hasNotificationPermissionGranted = true;
                // make your action here
            } else {
                String[] permissions = {Manifest.permission.POST_NOTIFICATIONS};
                ActivityCompat.requestPermissions(this, permissions, 1234);
            }
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // permission denied permanently
            } else {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            hasNotificationPermissionGranted = true;
        }
    }

    private void rxPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            hasLocationPermissionGranted = true;
        } else {
            String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 123);
        }
        notificationPermission();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } else {
            Log.e("TAG", "Permission not granted");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private float calculateTotalDistance(List<Location> locations) {
        float totalDistance = 0;

        for (int i = 1; i < locations.size(); i++) {
            totalDistance += locations.get(i - 1).distanceTo(locations.get(i));
        }

        return totalDistance;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermissionGranted = true;
            } else {
                hasLocationPermissionGranted = false;
            }
            notificationPermission();
        }
        if (requestCode == 1234) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                hasNotificationPermissionGranted = true;
            } else {
                hasNotificationPermissionGranted = false;
            }
        }
    }

    @Override
    public void totalDistanceGot(Double distance) {
        Double totalDistance = distance;
        Log.e("TAG", "Total Distance >>> " + String.format("Total Distance: %.2f kilometers", totalDistance));

        TextView txtView = (TextView) findViewById(R.id.total_distance_value);
        txtView.setText(String.format("%.2f", totalDistance) + " Km");
    }


    private double calculateTotalDistanceFromGoogle() throws IOException {
        // Build origins and destinations strings

        totalDistance = 0.0;

        ArrayList locations = new ArrayList<LocationEntity>();


        for(int i=0; i<OrderTimingService.Companion.getLocationList().size(); i++){
            Location location = OrderTimingService.Companion.getLocationList().get(i);
            LocationEntity locationEntity = new LocationEntity(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
            locations.add(locationEntity);
        }

        OkHttpClient client = new OkHttpClient();
        StringBuilder origins = new StringBuilder();
        StringBuilder destinations = new StringBuilder();
        for (int j=0; j<locations.size(); j++) {
            LocationEntity location = (LocationEntity) locations.get(j);
            if (origins.length() > 0) {
                origins.append("|");
                destinations.append("|");
            }
            origins.append(location.getLatitude()).append(",").append(location.getLongitude());
            destinations.append(location.getLatitude()).append(",").append(location.getLongitude());
        }

        // Make request to Google Distance Matrix API
        String url = BASE_URL + "?origins=" + origins.toString() + "&destinations=" + destinations.toString() + "&key=" + API_KEY;
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!response.isSuccessful()) {
            Log.e("TAG","response is not success");
        }
        String responseData = response.body().string();
        Log.d("TAG", "Response Data: " + responseData);
        JsonObject jsonObject = JsonParser.parseString(responseData).getAsJsonObject();
        JsonArray rows = jsonObject.getAsJsonArray("rows");

        // Parse the response to calculate the total distance
        for (int i = 0; i < rows.size() - 1; i++) {
            JsonObject row = rows.get(i).getAsJsonObject();
            JsonArray elements = row.getAsJsonArray("elements");
            JsonObject element = elements.get(i + 1).getAsJsonObject();
            JsonObject distance = element.getAsJsonObject("distance");
            totalDistance += distance.get("value").getAsDouble();
        }

        Log.e("TAG","Distance from google " + totalDistance);

        TextView txtView = (TextView) findViewById(R.id.total_distance_value);
        txtView.setText(totalDistance + " meter");

        return totalDistance / 1000;

    }

    private class CalculateDistanceFromGoogleTask extends AsyncTask<List<LocationEntity>, Void, Double> {

        @Override
        protected Double doInBackground(List<LocationEntity>... params) {
            List<LocationEntity> locations = params[0];
            try {
                return calculateTotalDistanceFromGoogle();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Double totalDistance) {
            if (totalDistance != null) {
                Log.d("TAG", "Total Distance: " + totalDistance + " km");
                // Handle the result as needed
            }
        }
    }
}

