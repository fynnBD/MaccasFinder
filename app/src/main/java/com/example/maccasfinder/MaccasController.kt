package com.example.maccasfinder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.maccasfinder.networking.networkInterface
import com.example.trademetest20.networking.ServiceBuilder
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MaccasController(val context: Context) : ViewModel() {
    private val TAG: String = MaccasController::class.java.getSimpleName()

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    private var locationSettingsRequest: LocationSettingsRequest? = null

    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

    val location = MutableLiveData<Location>()
    val target = MutableLiveData<Result>()

    fun startLocationTracking() : LiveData<Location>
    {
        locationRequest = LocationRequest()
        locationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        locationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)
        locationSettingsRequest = builder.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val currentLocation: Location = locationResult.lastLocation
                Log.i(TAG, "Location Callback results:")
                location.value = locationResult.lastLocation
                findNearest()
            }
        }



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            println("mega fail")
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 22)
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper())

        return location
    }

    private fun findNearest() {
        val locationValue = location.value
        val locationString = (locationValue?.latitude.toString()) + "," + locationValue?.longitude.toString()
        //val locationString = "-37.762937, 175.277677"
        val request = ServiceBuilder.buildService(networkInterface::class.java)
        val call = request.getId("AIzaSyA3YF8LR9zLoyS9BmWV58a27sDw6uNulF4",
                                    locationString, "McDonalds", "distance")

        call.enqueue(object : Callback<placeResults> {
            override fun onResponse(call: Call<placeResults>, response: Response<placeResults>) {
                if (response.isSuccessful) {
                    formatResults(response.body())
                } else {
                    Toast.makeText(context, "Fail!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<placeResults>, t: Throwable) {
                Toast.makeText(context, "Hard Fail", Toast.LENGTH_LONG).show()
                println("---------------------------------------------------------")
                t.printStackTrace()
                println("---------------------------------------------------------")
            }

        })
    }

    private fun formatResults(body: placeResults?) {
        if (body != null) {
            for(i in body.results)
            {
                if(i.name.contains("McDonald's"))
                {
                    this.target.value = i
                    println(i.name)
                    return
                }
                Toast.makeText(context, "No Maccas found nearby :(", Toast.LENGTH_LONG)
            }
        }
    }


}
