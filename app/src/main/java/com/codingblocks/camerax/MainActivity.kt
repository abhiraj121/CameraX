package com.codingblocks.camerax

import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executor


class MainActivity : AppCompatActivity(), Executor {

    var changeInLens:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            textureView.post {
                startCamera()
            }
        }else{
            //ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),1234)
        }

        //for changing the camera lens
        change.setOnClickListener {
            changeInLens==true
            Log.i("forImage","value changed to ${changeInLens}")
            startCamera()
        }
    }

    private fun startCamera() {
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            if (changeInLens==true) {
                setLensFacing(CameraX.LensFacing.FRONT)
            }else{
                setLensFacing(CameraX.LensFacing.BACK)
            }
            setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        //for capturing the image
        captureImage.setOnClickListener{
            val file = File(externalMediaDirs.first(),"${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file,this,object : ImageCapture.OnImageSavedListener{
                override fun onImageSaved(file: File) {
                    Log.i("forImage","Image Captured ${file.absolutePath}")
                }

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    cause: Throwable?
                ) {
                    Log.i("forImage","Error $message")

                }

            })
        }



        val previewConfig:PreviewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_16_9)
            if (changeInLens==true) {
                setLensFacing(CameraX.LensFacing.FRONT)
            }else{
                setLensFacing(CameraX.LensFacing.BACK)
            }
        }.build()
 
        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = textureView.parent as ViewGroup

            /* ToDo:ask about this */
            parent.removeView(textureView)
            parent.addView(textureView,0)
            updateTransform()
            textureView.surfaceTexture = it.surfaceTexture
        }

        CameraX.bindToLifecycle(this,preview,imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = textureView.width/2f
        val centerY = textureView.height/2f
        val rotationDegrees = when(textureView.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(),centerX,centerY)
        textureView.setTransform(matrix)
    }

    override fun execute(command: Runnable) {
        command.run()
    }
}
