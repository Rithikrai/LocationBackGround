package com.example.locationnew

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.locationnew.db.Locations
import com.example.locationnew.db.MainDatabase
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationService : Service() {
    private var context: Context = this
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var database: MainDatabase


    @SuppressLint("NotConstructor")
    fun LocationService(context: Context) {
        this.context = context
    }

    private val locationRequest: LocationRequest = create().apply {
        interval = 60000
        fastestInterval = 60000
        priority = PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60000
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                Toast.makeText(
                    this@LocationService, "Latitude: " + location.latitude.toString() + '\n' +
                            "Longitude: " + location.longitude, Toast.LENGTH_LONG
                ).show()
//                Log.d("Location d", location.latitude.toString())
                Log.i("Location i", location.latitude.toString())
                GlobalScope.launch {
                    database.locaton().insertAll(
                        Locations(
                            0,
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    )
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
            Notification()
        )

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            Toast.makeText(applicationContext, "Permission required", Toast.LENGTH_LONG).show()
            return
        } else {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val notificationChannelId = "Location channel id"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Location updating")
            .setSmallIcon(R.drawable.ic_locationupdate)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setProgress(0, 0, true)
            .setAutoCancel(false)
            .build()

        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        database =
            Room.databaseBuilder(context, MainDatabase::class.java, "LocationDB")
                .build()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}