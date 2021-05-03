package com.example.maccasfinder

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng

class CompassFragment(var location: Location, var target: LatLng) : Fragment(), SensorEventListener {
    lateinit var viewHolder : View

    private lateinit var sensorManager: SensorManager
    private lateinit var gsense : Sensor
    private lateinit var msense : Sensor

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val P = FloatArray(9)
    private val I = FloatArray(9)

    fun changeLocation(newLocation: Location)
    {
        location = newLocation
    }

    fun changeTarget(newTarget: LatLng)
    {
        target = newTarget
    }



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        var inflate = inflater.inflate(R.layout.compass_layout, container, false)
        viewHolder = inflate

        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsense = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        msense = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        start()

        return inflate
    }

    private fun start() {
        sensorManager.registerListener(this, gsense, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, msense, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val alpha = 0.97f

        synchronized(this) {
            if (event!!.sensor.type === Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity.get(0) + (1 - alpha) * event!!.values[0]
                mGravity[1] = alpha * mGravity.get(1) + (1 - alpha) * event!!.values[1]
                mGravity[2] = alpha * mGravity.get(2) + (1 - alpha) * event!!.values[2]

                // mGravity = event.values;

                // Log.e(TAG, Float.toString(mGravity[0]));
            }
            if (event!!.sensor.type === Sensor.TYPE_MAGNETIC_FIELD) {
                // mGeomagnetic = event.values;
                mGeomagnetic[0] = alpha * mGeomagnetic.get(0) + (1 - alpha) * event!!.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic.get(1) + (1 - alpha) * event!!.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic.get(2) + (1 - alpha) * event!!.values[2]
                // Log.e(TAG, Float.toString(event.values[0]));
            }

            val success = SensorManager.getRotationMatrix(P, I, mGravity,
                    mGeomagnetic)

            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(P, orientation)
                // Log.d(TAG, "azimuth (rad): " + azimuth);
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                println(azimuth)
                var angle =  angleFromCoordinate(location.latitude, location.longitude, target.latitude, target.longitude)

                rotateArrow(azimuth, angle)
                updateDist()

            }
        }
    }

    private fun angleFromCoordinate(lat1: Double, long1: Double, lat2: Double, long2: Double): Double {
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

    private fun rotateArrow(azimuth: Float, angle: Double) {
        var arrow = viewHolder.findViewById<ImageView>(R.id.arrow)
        arrow.rotation = (-azimuth-angle).toFloat()
    }

    private fun updateDist() {
        var dist = viewHolder.findViewById<TextView>(R.id.meters)
        dist.text = "About " + distance(location.latitude, location.longitude, target.latitude, target.longitude).toInt().toString() + "m"
    }

    fun distance(lat_a: Double, lng_a: Double, lat_b: Double, lng_b: Double): Float {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a).toDouble())
        val lngDiff = Math.toRadians((lng_b - lng_a).toDouble())
        val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a.toDouble())) * Math.cos(Math.toRadians(lat_b.toDouble())) *
                Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadius * c
        val meterConversion = 1609
        return (distance * meterConversion.toFloat()).toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


}