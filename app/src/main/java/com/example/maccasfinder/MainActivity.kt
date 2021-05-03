package com.example.maccasfinder

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng

class MainActivity : AppCompatActivity(), LocationListener {
    lateinit var compass : CompassFragment
    val defaultTarget = LatLng(-41.29032804837499, 174.77583166504266)
    lateinit var model : MaccasController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         model = MaccasController(this)
        val location = model.startLocationTracking()
        location.observe(this){location -> updateCompassPosition(location as Location)}
        model.target.observe(this){target -> updateCompassTarget(target as Result)}

    }

    fun updateCompassPosition(location: Location) {
        if(!this::compass.isInitialized)
        {
            if(model.target.value != null) {
                startCompass(location, model.target.value!!)
            }
        }
        else
        {
            compass.changeLocation(location)
        }
    }

    fun updateCompassTarget(target : Result)
    {
        if(!this::compass.isInitialized)
        {
            if(model.location.value != null) {
                startCompass(model.location.value!!, target)
            }
        }
        else
        {
            if(target != compass.target) {
                compass.changeTarget(target)
            }
        }
    }

    private fun startCompass(location: Location, target : Result) {
        compass = CompassFragment(location, target, "test")
        var ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.frame, compass!!).commit()
    }



    override fun onLocationChanged(location: Location) {
    }
}