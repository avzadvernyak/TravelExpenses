package m.kampukter.travelexpenses

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService

class CameraXService(private val cameraExecutor: ExecutorService) {

    private var imageCapture: ImageCapture? = null

    fun setUpCamera( lifecycleOwner: LifecycleOwner, context: Context, viewFinder: PreviewView ) {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build()

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )

                // Attach the viewfinder's surface provider to preview use case
                preview.setSurfaceProvider(viewFinder.surfaceProvider)

            } catch (exc: Exception) {
                Log.e("blabla", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }
    fun takePhoto( file: File, onSavePhotoUriToExpenses: ((String) -> Unit)) {
        imageCapture?.let { _imageCapture ->

            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            _imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(file)
                        onSavePhotoUriToExpenses.invoke(savedUri.toString())
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(
                            "blabla",
                            "Error (OnImageSavedCallback): ${exception.message}"
                        )
                    }
                })

        }
    }
}