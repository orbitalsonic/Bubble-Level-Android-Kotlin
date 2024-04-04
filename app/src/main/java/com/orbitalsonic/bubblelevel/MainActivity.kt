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

    private var lastSensorUpdateTime: Long = 0
    private val alpha = 0.96f
    private val updateInterval = 10L
    private val gravityValues = FloatArray(3)
    private val magneticValues = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        acmView = findViewById(R.id.acm_view)

        initSensor()
    }

    private fun initSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        startSensors()
    }

    private fun startSensors() {
        val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (gravitySensor == null || magneticSensor == null) {
            sensorNotSupported()
        } else {
            sensorManager?.apply {
                registerListener(this@MainActivity, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
                registerListener(this@MainActivity, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun sensorNotSupported() {
        Toast.makeText(this, "Sensors Not Supported in this device", Toast.LENGTH_SHORT).show()
    }

    private fun stopSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        startSensors()
    }

    override fun onPause() {
        super.onPause()
        stopSensors()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSensorUpdateTime < updateInterval) return

        lastSensorUpdateTime = currentTime

        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> {
                gravityValues[0] = event.values[0]
                gravityValues[1] = event.values[1]
                gravityValues[2] = event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticValues[0] = alpha * magneticValues[0] + (1 - alpha) * event.values[0]
                magneticValues[1] = alpha * magneticValues[1] + (1 - alpha) * event.values[1]
                magneticValues[2] = alpha * magneticValues[2] + (1 - alpha) * event.values[2]
            }
        }

        updateBallRotation()
    }

    private fun updateBallRotation() {
        val rotationMatrix = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

            acmView.updateOrientation(pitch, roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}