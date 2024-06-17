package com.example.locationapplication

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.locationapplication.test.TestActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Dispatcher

import java.io.IOException;
import java.util.List;


class OrderTimingService : Service() {

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "countdown_channel"

    private var countDownTimer: CountDownTimer? = null
    private var notificationLayout: RemoteViews? = null

    private var runninOrderTime:String = "60"
    private var orderId: String ? = null
    private var isFirstOrder: String ?= null

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
//    private val CHANNEL_ID = "location_service_channel"


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
//        notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        if(Build.VERSION.SDK_INT > 33) {
            ServiceCompat.startForeground(
                this,
                1,
                createNotification("00:00"),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        }
        else{
            startForeground(
                NOTIFICATION_ID,
                createNotification("00:00"))
        }

        Log.e("Service", "Service has been started")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }

                if(!OPEN_FOR_GOOGLE){
                        for(location in locationResult.locations){
                            Log.e(
                                "TAG", "Locatio got on change >>> " + locationResult.lastLocation!!
                                    .latitude
                            )
                            locationList.add(location)
                            getTotalDistanceInKilometer()
                            // Handle each location update
                            saveLocation(locationResult.lastLocation!!)
                        }
                }
                else{
                    if(locationResult.lastLocation != null) {
                        Log.e(
                            "TAG", "Locatio got on change >>> " + locationResult.lastLocation!!
                                .latitude
                        )
                        locationList.add(locationResult.lastLocation!!)
//                        getTotalDistanceInKilometer()
                        // Handle each location update
                        saveLocation(locationResult.lastLocation!!)
                    }
                }
//
//                for (location in locationResult.locations) {

//                }
                // Optionally, update the UI with the new location
            }
        }

        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent.let {
            orderId = it?.getStringExtra(ORDER_ID)
            isFirstOrder = it?.getStringExtra(IS_FIRST_ORDER)
        }
        return START_STICKY
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MyLocationClass::class.java)
        notificationIntent.putExtra(OPEN_FROM_NOTIFICATION, true)
        notificationIntent.putExtra(
            OPEN_FROM_NOTIFICATION_VALUE,
            OEPN_FOR_RUNNING_ORDER
        )
        notificationIntent.putExtra(RUNNING_ORDER_TIME, runninOrderTime)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

//        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
//        notificationLayout.setTextViewText(R.id.timerTextView, contentText)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
//            .setSmallIcon(R.drawable.ic_notification_icon)
//            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(null)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Location Service")
            .setContentText("Fetching Location service")
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Countdown Channel"
            val descriptionText = "Channel for countdown service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

//    private fun startCountdown() {
//        countDownTimer = object : CountDownTimer(60000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                val seconds = millisUntilFinished / 1000
//                runninOrderTime = seconds.toString()
//                val timerText = String.format("%02d:%02d", seconds / 60, seconds % 60)
//                notificationLayout!!.setTextViewText(R.id.timerTextView, timerText)
//                EventBus.getDefault().postSticky(StatusEvent(StatusEnum.ORDER_RUNNING_TIME, timerText))
//                updateNotification(timerText)
//            }
//
//            override fun onFinish() {
//                val timerText = String.format("%02d:%02d", 0, 0)
//                notificationLayout!!.setTextViewText(R.id.timerTextView, timerText)
//                updateNotification(timerText)
//                EventBus.getDefault().postSticky(StatusEvent(StatusEnum.HIDE_ORDER_RUNNING_TIME, 0))
//                EventBus.getDefault().postSticky(StatusEvent(StatusEnum.CART_MODIFIED))
//                // Timer finished
//                SharedHelper(AppController.getInstance()).putString("",SharedHelper.RUNNING_ORDER_ID)
//                SharedHelper(AppController.getInstance()).putString("",SharedHelper.RUNNING_ORDER_IS_FIRST)
//                stopSelf()
//            }
//        }.start()
//    }

    private fun updateNotification(timerText: String) {
        val notification = createNotification(timerText)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().postSticky(StatusEvent(StatusEnum.HIDE_ORDER_RUNNING_TIME, 0))
//        EventBus.getDefault().postSticky(StatusEvent(StatusEnum.CART_MODIFIED))
//        countDownTimer?.cancel()
        Log.e("TAG","Service onDestroy() called")
        locationList = ArrayList();
        totalDistance = 0.0;
        fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ORDER_ID = "order_id"
        const val IS_FIRST_ORDER = "is_first_order"
        const val OPEN_FROM_NOTIFICATION = "open from"
        const val OPEN_FROM_NOTIFICATION_VALUE = "open from notification value"
        const val OEPN_FOR_RUNNING_ORDER = "open from notification value"
        const val RUNNING_ORDER_TIME = "running order time"
        var locationList: ArrayList<Location> = ArrayList()
        var totalDistance = 0.0
        lateinit var  mainActivityReeference : MyLocationClass
        var OPEN_FOR_GOOGLE = false

        open fun setOpenForGoogle(value: Boolean){
            OPEN_FOR_GOOGLE = value;
        }

        open fun startService(context: Context, intent: Intent) {
//            val intent = Intent(context, OrderTimingService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        open fun getTotalDistanceInMeters(): Double{

            for (i in 1 until locationList.size) {
                totalDistance += locationList.get(i - 1).distanceTo(locationList.get(i))
            }

            var resultDistance = totalDistance

            return resultDistance / 1000
        }

        open fun setCallBack(callback : MyLocationClass ){
            mainActivityReeference = callback
        }

        open fun getTotalDistanceInKilometer(): Double{

            for (i in 1 until locationList.size) {
//                var distance = calculateDistance(locationList.get(locationList.size-2).latitude, locationList.get(locationList.size-2).longitude, locationList.get(locationList.size-1).latitude, locationList.get(locationList.size-1).longitude)
//                distance = distance * 1000
                var distance = locationList.get(i-1).distanceTo(locationList.get(i))
                Log.e("TAG","Distance get in meters ${distance}")
                if(distance > 100) {
                    totalDistance += distance
                }
            }

            mainActivityReeference.totalDistanceGot(totalDistance/1000)

            return totalDistance/1000
        }

        open fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {

            val a = 6378137.0 // Semi-major axis in meters
            val f = 1 / 298.257223563 // Flattening
            val b = a * (1 - f) // Semi-minor axis

            val L = Math.toRadians(lon2 - lon1)
            val U1 = atan((1 - f) * tan(Math.toRadians(lat1)))
            val U2 = atan((1 - f) * tan(Math.toRadians(lat2)))

            val sinU1 = sin(U1)
            val cosU1 = cos(U1)
            val sinU2 = sin(U2)
            val cosU2 = cos(U2)

            var lambda = L
            var lambdaP: Double
            var iterLimit = 100.0
            var cosSqAlpha: Double
            var sinSigma: Double
            var cosSigma: Double
            var sigma: Double
            var cos2SigmaM: Double
            var sinLambda: Double
            var cosLambda: Double

            do {
                sinLambda = sin(lambda)
                cosLambda = cos(lambda)
                sinSigma = sqrt(
                    cosU2 * sinLambda * (cosU2 * sinLambda) +
                            (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                )
                if (sinSigma == 0.0) {
                    return 0.0 // co-incident points
                }
                cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
                sigma = atan2(sinSigma, cosSigma)
                val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
                cosSqAlpha = 1 - sinAlpha * sinAlpha
                cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha
                if (java.lang.Double.isNaN(cos2SigmaM)) {
                    cos2SigmaM = 0.0 // equatorial line
                }
                val C: Double = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha))
                lambdaP = lambda
                lambda =
                    L + (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)))
            } while (abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0)

            if (iterLimit == 0.0) {
                return Double.NaN // formula failed to converge
            }

            val uSq: Double = cosSqAlpha * (a * a - b * b) / (b * b)
            val A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)))
            val B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)))
            val deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 *
                    (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)))

            val s: Double = b * A * (sigma - deltaSigma)

            return s / 1000
        }

        private val API_KEY = "AIzaSyDGgHm62cLziXwWGsVPhuI68u1GpvcE-XA"
        private val BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json"

        @Throws(IOException::class)
        open fun calculateTotalDistanceFromGoogle(): Double {
            val locations = ArrayList<LocationEntity>()
            for(location in locationList){
                val locationEntity = LocationEntity(location.latitude, location.longitude, System.currentTimeMillis())
                locations.add(locationEntity)
            }

            val client = OkHttpClient()

            // Build origins and destinations strings
            val origins = StringBuilder()
            val destinations = StringBuilder()
            for (location in locations) {
                if (origins.length > 0) {
                    origins.append("|")
                    destinations.append("|")
                }
                origins.append(location.latitude).append(",").append(location.longitude)
                destinations.append(location.latitude).append(",")
                    .append(location.longitude)
            }

            // Make request to Google Distance Matrix API
            val url = "$BASE_URL?origins=$origins&destinations=$destinations&key=$API_KEY"
            val request: Request = Request.Builder().url(url).build()

            Dispatcher().run {
                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                val responseData: String = response.body.toString()
                val jsonObject = JsonParser.parseString(responseData).getAsJsonObject()
                val rows = jsonObject.getAsJsonArray("rows")

                var totalDistance = 0.0

                // Parse the response to calculate the total distance
                for (i in 0 until rows.size() - 1) {
                    val row = rows[i].getAsJsonObject()
                    val elements = row.getAsJsonArray("elements")
                    val element = elements[i + 1].getAsJsonObject()
                    val distance = element.getAsJsonObject("distance")
                    totalDistance += distance["value"].asDouble
                }

                // Convert meters to kilometers
                return totalDistance / 1000

            }
        }

    }

    private fun startLocationUpdates() {
//        val locationRequest = LocationRequest.create()
//        locationRequest.setInterval(15000)
//        locationRequest.setFastestInterval(15000)
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//        locationRequest.setSmallestDisplacement(100)
//        locationRequest.setWaitForAccurateLocation(true)
        var locationRequestBuilder: LocationRequest.Builder? = null

        locationRequestBuilder = LocationRequest.Builder(15000)
        locationRequestBuilder.setMinUpdateDistanceMeters(100f)
            locationRequestBuilder.setIntervalMillis(15000)
        locationRequestBuilder.setWaitForAccurateLocation(true)

        val locationRequest = locationRequestBuilder.build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    private fun saveLocation(location: Location) {}

}