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

    private lateinit var acmView: AccelerometerView

    private var sensorManager: SensorManager? = null

    private val sGravityValues = FloatArray(3)
    private val sMagneticValues = FloatArray(3)

    private var acmPrevTime: Long = 0
    private val mAlpha = 0.96f
    private val updateInterval = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        acmView = findViewById(R.id.acm_view)

        initSensor()
    }

    private fun initSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
    }

    private fun setBallRotation(time: Long) {
        val r = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, null, sGravityValues, sMagneticValues)) {
            var orientation = FloatArray(3)
            orientation = SensorManager.getOrientation(r, orientation)
            if (time - acmPrevTime > updateInterval) {
                Math.toDegrees(orientation[0].toDouble()).toFloat()  //azimuth
                Math.toDegrees(orientation[1].toDouble()).toFloat()  //pitch
                Math.toDegrees(orientation[2].toDouble()).toFloat() //roll
                acmPrevTime = time
                acmView.updateOrientation(
                    Math.toDegrees(orientation[1].toDouble()).toFloat(),
                    Math.toDegrees(orientation[2].toDouble()).toFloat()
                )
            }
        }
    }

    private fun sensorNotSupported() {
        Toast.makeText(this, "Sensors Not Supported in this device", Toast.LENGTH_SHORT).show()
    }

    private fun startSensors() {
        val mSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val aSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val rSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val gSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        if (mSensor == null || aSensor == null) {
            sensorNotSupported()
            return
        } else {
            sensorManager?.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, rSensor, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager?.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }

    }

    private fun stopSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        synchronized(this) {
            val time = System.currentTimeMillis()
            event?.let { mEvent ->
                if (mEvent.sensor.type == Sensor.TYPE_GRAVITY) {
                    sGravityValues[0] = mEvent.values[0]
                    sGravityValues[1] = mEvent.values[1]
                    sGravityValues[2] = mEvent.values[2]
                    setBallRotation(time)
                }

                if (mEvent.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    sMagneticValues[0] =
                        mAlpha * sMagneticValues[0] + (1 - mAlpha) * mEvent.values[0]
                    sMagneticValues[1] =
                        mAlpha * sMagneticValues[1] + (1 - mAlpha) * mEvent.values[1]
                    sMagneticValues[2] =
                        mAlpha * sMagneticValues[2] + (1 - mAlpha) * mEvent.values[2]
                    setBallRotation(time)
                }
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        startSensors()
    }

    override fun onPause() {
        super.onPause()
        stopSensors()
    }
}