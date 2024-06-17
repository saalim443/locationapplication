package com.example.locationapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final String CHANNEL_ID = "location_service_channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Service","Service has been started");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.e("TAG","Locatio got on change >>> " + locationResult.getLastLocation().getLatitude());
                        // Handle each location update
                        saveLocation(location);
                    }
                    // Optionally, update the UI with the new location
            }
        };

        createNotificationChannel();
        startForegroundService();
        startLocationUpdates();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService() {
//        NotificationChannel channel = new NotificationChannel(
//                CHANNEL_ID,
//                "Location Service Channel",
//                NotificationManager.IMPORTANCE_HIGH
//
//        );
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking your location")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        //        ServiceCompat.startForeground(
//                this,
//                1,
//                notification,
//                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
//        );
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        }

    }

    private void saveLocation(Location location) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
