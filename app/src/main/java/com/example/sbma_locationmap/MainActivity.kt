package com.example.sbma_locationmap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocatinoClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission()

        setContent {
            Text("Location Map Lab")
        }

        fusedLocatinoClient = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocatinoClient.lastLocation.addOnSuccessListener {
                Log.i("pengb", "latitude: ${it.latitude}, longitude: ${it.longitude}, etc: $it")
            }
        }

        val locationCallback = object: LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                for(location in p0.locations){
                    Log.d("pengb",
                        "latitude: ${location.latitude} longitude: ${location.longitude} etc: $location")
                }
            }
        }

        val locationRequest = LocationRequest
            .create()
            .setInterval(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        fusedLocatinoClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

    }


    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), 100
        )
    }
}