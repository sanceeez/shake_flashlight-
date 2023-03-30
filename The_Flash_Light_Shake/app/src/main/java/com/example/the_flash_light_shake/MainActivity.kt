package com.example.the_flash_light_shake

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.the_flash_light_shake.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private var torchState: Boolean= false
    private var IdCam:String= "0"
    private var SOSpattern = intArrayOf(200,200,200,600,600,600,200,200,200 )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val constraintLayout: ConstraintLayout = binding.MainLayout
        val animationDrawable: AnimationDrawable = constraintLayout.background as AnimationDrawable
            animationDrawable.setEnterFadeDuration(2500)
            animationDrawable.setExitFadeDuration(5000)
            animationDrawable.start()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        IdCam = cameraManager.cameraIdList[0]
        Dexter.withContext(this).withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    TurnFlashLight()
                }
                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@MainActivity,
                        "Dear user please allow the permission",
                        Toast.LENGTH_LONG
                    ).show()
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
        binding.sos.setOnClickListener {
            sosRequest()
        }
    }
    private fun  sosRequest() {

        if (torchState) {
            cameraManager.setTorchMode(IdCam,true)
            binding.imageView.setImageResource(R.drawable.flashlight_on)
            torchState=true}
            val pattern = SOSpattern.clone()
            for (i in pattern.indices) {
                pattern[i]*=4
            }
            val handler = Handler()
            Thread{
                for (i in pattern.indices){
                    handler.post{
                        cameraManager.setTorchMode(IdCam,true)
                        binding.imageView.setImageResource(R.drawable.flashlight_on)
                    }
            Thread.sleep(pattern[i].toLong())
                    handler.post{
                        cameraManager.setTorchMode(IdCam,false)
                        binding.imageView.setImageResource(R.drawable.flashlight_off)
                    }
            Thread.sleep(100)
                }
            }.start()
    }
    private fun TurnFlashLight() {
    binding.imageView.setOnClickListener{
            torchState = when(torchState){
                false->{
                    cameraManager.setTorchMode(IdCam,true)
                    binding.imageView.setImageResource(R.drawable.flashlight_on)
                    true }
                true->{

            cameraManager.setTorchMode(IdCam, false)
                    binding.imageView.setImageResource(R.drawable.flashlight_off)
                    false
                }
            }
    }
    }
    override fun onResume() {
        super.onResume()
        sensor?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // do nothing
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = Math.sqrt((x*x+y*y+z*z).toDouble())

            if (acceleration > 25){
                torchState = when(torchState){
                    false->{
                        cameraManager.setTorchMode(IdCam,true)
                        binding.imageView.setImageResource(R.drawable.flashlight_on)
                        true }
                    true->{
                        cameraManager.setTorchMode(IdCam, false)
                        binding.imageView.setImageResource(R.drawable.flashlight_off)
                        false
                    }
                }
            }
            }
        }
    }


