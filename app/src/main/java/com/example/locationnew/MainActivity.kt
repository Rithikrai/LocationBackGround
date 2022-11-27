package com.example.locationnew

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.room.Room
import com.example.locationnew.databinding.ActivityMainBinding
import com.example.locationnew.db.MainDatabase
import com.google.android.gms.location.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    var mLocationService: LocationService = LocationService()
    lateinit var mServiceIntent: Intent
    lateinit var telephonyManager: TelephonyManager
    lateinit var database: MainDatabase
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var Lat: String? = "0.0"
    var Long: String? = "0.0"
    private var PERMISSION_ID = 44
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        getLastLocation()

        database = Room.databaseBuilder(applicationContext, MainDatabase::class.java, "LocationDB")
            .build()


        binding.viewbtn.setOnClickListener {
            database.locaton().getContact().observe(this, {
                binding.tv.setText(it.toString())
            })
        }

        binding.startServiceBtn.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {

                        val builder = AlertDialog.Builder(this)
                        setTitle("Background permission")
                        builder.setMessage(R.string.backgroundpermission)
                        builder.setPositiveButton("Start service anyway") { dialog: DialogInterface, which: Int ->
                            starServiceFunc()

                        }
                        builder.setNegativeButton(
                            "Grant background Permission"
                        ) { dialog: DialogInterface, which: Int -> requestBackgroundLocationPermission() }
                        builder.show()

                    } else if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        starServiceFunc()
                    }
                } else {
                    starServiceFunc()
                }

            } else if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    AlertDialog.Builder(this)
                        .setTitle("ACCESS_FINE_LOCATION")
                        .setMessage("Location permission required")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            requestFineLocationPermission()
                        }
                        .create()
                        .show()
                } else {
                    requestFineLocationPermission()
                }
            }

        }

        binding.stopServiceBtn.setOnClickListener {
            stopServiceFunc()
        }
    }

    private fun starServiceFunc() {
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            startService(mServiceIntent)

            Toast.makeText(this, getString(R.string.running), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, getString(R.string.alreadyrunning), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopServiceFunc() {
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            stopService(mServiceIntent)
            Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show()
        }
    }

    //=========================================TO STOP SERVICES WHEN APP CLOSED/DESTORY===========================
    override fun onDestroy() {
        /*  if (::mServiceIntent.isInitialized) {
              stopService(mServiceIntent)
          }*/
        super.onDestroy()
    }
    //============================================================================================================
    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), MY_BACKGROUND_LOCATION_REQUEST
        )
    }

    private fun requestFineLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_FINE_LOCATION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Toast.makeText(this, requestCode.toString(), Toast.LENGTH_LONG).show()
        when (requestCode) {
            MY_FINE_LOCATION_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        requestBackgroundLocationPermission()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "ACCESS_FINE_LOCATION permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
            MY_BACKGROUND_LOCATION_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this,
                            "Background location Permission Granted",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG)
                        .show()
                }
                return
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    private fun getLastLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        Lat = location.latitude.toString()
                        Long = location.longitude.toString()
                    }
                }
                getDeviceId()
            } else {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Turn on Location")
                    .setMessage("Turn on location service to allow Our App to determine your location")
                    .setCancelable(false)
                    .setPositiveButton(
                        "Turn On"
                    ) { dialog, which ->
                        Toast.makeText(applicationContext, "Turn on location", Toast.LENGTH_LONG)
                            .show()
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, 2011)
                    }
                    .show()
            }
        } else {
            requestPermissions()
        }
    }

    private fun getDeviceId() {
        telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                101
            )
            return
        }
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Lat = mLastLocation.latitude.toString()
            Long = mLastLocation.longitude.toString()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                PERMISSION_ID
            )
        }
    }

    companion object {
        private const val MY_FINE_LOCATION_REQUEST = 99
        private const val MY_BACKGROUND_LOCATION_REQUEST = 100
    }


}