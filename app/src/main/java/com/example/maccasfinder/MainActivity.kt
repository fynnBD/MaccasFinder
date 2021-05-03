package com.example.maccasfinder

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {
    lateinit var compass : CompassFragment
    val defaultTarget = LatLng(-41.29032804837499, 174.77583166504266)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var model = MaccasController(this)
        val location = model.startLocationTracking()
        location.observe(this){location -> updateCompassPosition(location as Location)}

    }

    fun updateCompassPosition(location: Location) {
        if(!this::compass.isInitialized)
        {
            startCompass(location)
        }
        else
        {
            compass.changeLocation(location)
        }
    }

    fun updateCompassTarget(target : LatLng)
    {

    }

    private fun startCompass(location: Location) {
        println("AH SHIT FUCk")
        compass = CompassFragment(location, defaultTarget)
        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame, compass!!).commit()
    }



    override fun onLocationChanged(location: Location) {
        println("Shit")
    }
}