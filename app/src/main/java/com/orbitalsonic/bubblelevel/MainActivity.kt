package com.orbitalsonic.bubblelevel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.orbitalsonic.bubblelevel.widget.AccelerometerView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var acceleroMeterView: AccelerometerView

    private var sensorManager: SensorManager? = null

    private var mSensor: Sensor? = null
    private var aSensor: Sensor? = null
    private var rSensor: Sensor? = null
    private var gSensor: Sensor? = null

    private val rotationMatrixR = FloatArray(9)
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var orientation = FloatArray(3)

    private var accelerometerPrevTime: Long = 0
    private val alpha = 0.96f
    private val updateInterval = 10 //mills


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        acceleroMeterView = findViewById(R.id.acm_view)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        mSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        aSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        rSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)

    }

    override fun onSensorChanged(event: SensorEvent?) {

        synchronized(this) {
            val time = System.currentTimeMillis()

            event?.let { mEvent ->
                if (mEvent.sensor.type == Sensor.TYPE_GRAVITY) {
                    if (gSensor != null) {
                        gravity[0] = mEvent.values[0]
                        gravity[1] = mEvent.values[1]
                        gravity[2] = mEvent.values[2]
                        updateOrientation(time)
                    }
                }


                if (mEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    if (mEvent.sensor.type == Sensor.TYPE_GRAVITY) {
                        if (gSensor == null) {
                            gravity[0] = this.alpha * gravity[0] + (1 - this.alpha) * mEvent.values[0]
                            gravity[1] = this.alpha * gravity[1] + (1 - this.alpha) * mEvent.values[1]
                            gravity[2] = this.alpha * gravity[2] + (1 - this.alpha) * mEvent.values[2]
                            updateOrientation(time)
                        }
                    }

                }

                if (mEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {

                    geomagnetic[0] = this.alpha * geomagnetic[0] + (1 - this.alpha) * mEvent.values[0]
                    geomagnetic[1] = this.alpha * geomagnetic[1] + (1 - this.alpha) * mEvent.values[1]
                    geomagnetic[2] = this.alpha * geomagnetic[2] + (1 - this.alpha) * mEvent.values[2]


                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun startSensors() {
        mSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        aSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        rSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if (mSensor == null || aSensor == null) {
            notSupported()
            return
        } else {
            sensorManager?.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, rSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun updateOrientation(time: Long) {
        if (SensorManager.getRotationMatrix(rotationMatrixR, null, gravity, geomagnetic)) {
            orientation = SensorManager.getOrientation(rotationMatrixR, orientation)
            if (time - accelerometerPrevTime > updateInterval) {
                Math.toDegrees(orientation[0].toDouble()).toFloat()  //azimuth
                Math.toDegrees(orientation[1].toDouble()).toFloat()  //pitch
                Math.toDegrees(orientation[2].toDouble()).toFloat() //roll
                accelerometerPrevTime = time
                acceleroMeterView.updateOrientation(
                    Math.toDegrees(orientation[1].toDouble()).toFloat(),
                    Math.toDegrees(orientation[2].toDouble()).toFloat()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    private fun notSupported() {
        Toast.makeText(this, "Sensors Not Supported in this device", Toast.LENGTH_SHORT).show()
    }
}