package com.example.sbma_locationmap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.example.sbma_locationmap.MainActivity.Companion.address
import com.example.sbma_locationmap.MainActivity.Companion.altitude
import com.example.sbma_locationmap.MainActivity.Companion.latitude
import com.example.sbma_locationmap.MainActivity.Companion.longitude
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocatinoClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    companion object{
        val latitude = mutableStateOf(60.17)
        val longitude = mutableStateOf(24.95)
        var altitude = 0.0
        var address = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermission()

        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )

        geocoder = Geocoder(this, Locale.getDefault())

        setContent {
            ShowMap()
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
                val location = p0.lastLocation
                latitude.value = location?.latitude ?: latitude.value
                longitude.value = location?.longitude?: longitude.value
                altitude = location?.altitude?: altitude
                address = geocoder.getFromLocation(latitude.value, longitude.value, 1)[0].getAddressLine(0)
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
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
    }
}

@Composable
fun composeMap(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }
    return mapView
}

@Composable
fun ShowMap() {
    val map = composeMap()
    var mapInitialized by remember(map) { mutableStateOf(false)}
    val marker = Marker(map)

    val source = object: OnlineTileSourceBase(
        "USGS Topo", 0, 8,
        256, ".png",
        arrayOf("https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer/tile/")){
        override fun getTileURLString(pMapTileIndex: Long): String {
            return baseUrl +
                    MapTileIndex.getZoom(pMapTileIndex) +
                    "/" + MapTileIndex.getY(pMapTileIndex) +
                    "/" + MapTileIndex.getX(pMapTileIndex) +
                    mImageFilenameEnding
        }
    }

    val source2 = object: XYTileSource(
        "HOT", 1, 20,
        256, ".png",
        arrayOf(
            "http://a.tile.openstreetmap.fr/",
            "http://b.tile.openstreetmap.fr/",
            "http://c.tile.openstreetmap.fr/"
        ),
        "© OpenStreetMap contributors"
    ){}

    Log.d("pengb", source.getTileURLString(1))

    map.setTileSource(TileSourceFactory.OpenTopo)

    if(!mapInitialized){

        map.controller.setZoom(17.0)
        map.controller.setCenter(GeoPoint(latitude.value, longitude.value))
    }

    map.overlays.clear()

    AndroidView({map}){
        Log.d("pengb", "JHBHNBNBKJNB")
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.position = GeoPoint(latitude.value, longitude.value)
        marker.closeInfoWindow()
        marker.title = "$address ${latitude.value} ${longitude.value} $altitude"
        map.overlays.add(marker)
        map.invalidate()
    }
}