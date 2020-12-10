package uz.pdp.pedomaster

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import library.minimize.com.chronometerpersist.ChronometerPersist

class MainActivity : AppCompatActivity(), SensorEventListener, StepListener {

    private var simpleStepDetector: StepDetector? = null
    private var sensorManager: SensorManager? = null
    private var numSteps: Int = 0

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector!!.updateAccelerometer(
                p0.timestamp,
                p0.values[0],
                p0.values[1],
                p0.values[2]
            )
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun step(timeNs: Long) {
        numSteps++
        text_start.text = numSteps.toString()
        val km = forKm(numSteps)
        km_size.text = km.toString()
        val forkkal = forkkal(numSteps)
        kkal.text = forkkal.toString()
    }

    private fun forkkal(step: Int): Float {
        var caloriesCount = 0.0f
        for (i in 0 until step) {
            caloriesCount += 0.03485f
        }
        return caloriesCount
    }

    private fun forKm(step: Int): Float {
        return step / 1400.0f
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPreferences = this.getSharedPreferences("time", Context.MODE_PRIVATE)

        val chronometer = ChronometerPersist
        val instance = chronometer.getInstance(time_chrono, "mainChronometer", sharedPreferences)
        instance.hourFormat(true)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        simpleStepDetector = StepDetector()
        simpleStepDetector!!.registerListener(this)
        numSteps = 0

        instance.stopChronometer()
        var isStart = true

        start_btn.setOnClickListener {
            if (isStart) {
                sensorManager!!.registerListener(
                    this,
                    sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                isStart = false
                instance.startChronometer()
                castText.text = "Pause"
            } else if (isStart == false) {
                sensorManager!!.unregisterListener(this)
                instance.pauseChronometer()
                isStart = true
                castText.text = "Start"
            }
        }

        start_btn.setOnLongClickListener {
            isStart = true
            instance.stopChronometer()
            numSteps = 0
            forKm(0)
            forkkal(0)
            km_size.text = "0.00"
            kkal.text = 0.0.toString()
            text_start.text = 0.toString()
            castText.text = "Start"
            Toast.makeText(this, "Success Reset Data", Toast.LENGTH_SHORT).show()
            true
        }
    }
}