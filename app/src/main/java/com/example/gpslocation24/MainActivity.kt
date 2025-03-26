package com.example.gpslocation24

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), LocationListener {
    private val LOCATION_PERM_CODE = 2
    private lateinit var locationManager: LocationManager
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        statusText = findViewById(R.id.statusText)

        checkLocationPermission()

        findViewById<Button>(R.id.updButton).setOnClickListener {
            requestLocationUpdate()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERM_CODE)
        } else {
            requestLocationUpdate()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            updateStatus(false)
            return
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        val provider = locationManager.getBestProvider(Criteria(), true)
        provider?.let {
            val location = locationManager.getLastKnownLocation(it)
            location?.let { loc ->
                displayCoord(loc.latitude, loc.longitude)
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        displayCoord(location.latitude, location.longitude)
        Log.d("my", "lat ${location.latitude} long ${location.longitude}")
    }

    private fun displayCoord(latitude: Double, longitude: Double) {
        findViewById<TextView>(R.id.lat).text = String.format("%.5f", latitude)
        findViewById<TextView>(R.id.lng).text = String.format("%.5f", longitude)
        updateStatus(true)  // ГПС работает
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERM_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdate()
            } else {
                statusText.text = "Permission Denied"
            }
        }
    }

    override fun onProviderDisabled(provider: String) {
        updateStatus(false)
    }

    override fun onProviderEnabled(provider: String) {
        updateStatus(true)
    }

    private fun updateStatus(isOnline: Boolean) {
        statusText.text = if (isOnline) "Online" else "Offline"
        statusText.setTextColor(if (isOnline) ContextCompat.getColor(this, android.R.color.holo_green_dark)
        else ContextCompat.getColor(this, android.R.color.holo_red_dark))
    }
}
