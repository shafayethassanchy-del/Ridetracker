package com.ridefuel.tracker.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.ridefuel.tracker.R
import com.ridefuel.tracker.ui.MapActivity

/**
 * Foreground Service to handle real-time GPS tracking even when the app is in background.
 */
class TrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        const val NOTIFICATION_ID = 1
        
        // LiveData to observe from UI
        val isTracking = MutableLiveData<Boolean>(false)
        val pathPoints = MutableLiveData<MutableList<Location>>(mutableListOf())
        val totalDistance = MutableLiveData<Float>(0f) // in meters
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                "ACTION_START_TRACKING" -> startForegroundService()
                "ACTION_STOP_TRACKING" -> stopTracking()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        isTracking.postValue(true)
        createNotificationChannel()
        
        val notificationIntent = Intent(this, MapActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("RideFuel Tracker")
            .setContentText("Tracking your trip...")
            .setSmallIcon(R.drawable.ic_bike)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value == true) {
                result.locations.forEach { location ->
                    updatePathPoints(location)
                }
            }
        }
    }

    private fun updatePathPoints(location: Location) {
        val currentPoints = pathPoints.value ?: mutableListOf()
        
        if (currentPoints.isNotEmpty()) {
            val lastLocation = currentPoints.last()
            val distance = lastLocation.distanceTo(location)
            totalDistance.postValue((totalDistance.value ?: 0f) + distance)
        }
        
        currentPoints.add(location)
        pathPoints.postValue(currentPoints)
    }

    private fun stopTracking() {
        isTracking.postValue(false)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
