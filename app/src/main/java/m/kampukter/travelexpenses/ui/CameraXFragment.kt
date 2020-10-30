package m.kampukter.travelexpenses.ui

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri.fromFile
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.camerax_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.camerax_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        context?.let { context ->
            val isLocationPermission = permissionsForCamera.all { permission ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }
            if (!isLocationPermission) {
                // Permission is don't granted
                findNavController().navigate(R.id.toCameraPermissionsDialogFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        viewFinder.post {
            setUpCamera(view.context)
        }
        viewModel.bufferExpensesMediatorLiveData.observe(viewLifecycleOwner, { value ->
            if (!value.first?.imageUri.isNullOrEmpty()) findNavController().navigate(R.id.next_action)
        })
        camera_capture_button.setOnClickListener {
            viewModel.createJPGFile()?.let { takePhoto(it) }
        }

    }

    private fun setUpCamera(context: Context) {
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
                    this, cameraSelector, preview, imageCapture
                )

                // Attach the viewfinder's surface provider to preview use case
                preview.setSurfaceProvider(viewFinder.surfaceProvider)

            } catch (exc: Exception) {
                Log.e("blabla", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto( file: File ) {
        imageCapture?.let { _imageCapture ->

                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                _imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri = outputFileResults.savedUri ?: fromFile(file)
                            viewModel.setBufferExpensesPhoto(savedUri.toString())
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}