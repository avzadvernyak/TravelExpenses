package m.kampukter.travelexpenses.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.camerax_fragment.*
import kotlinx.android.synthetic.main.main_activity.*
import m.kampukter.travelexpenses.CameraXService
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

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

        with(activity as AppCompatActivity) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            supportActionBar?.hide()
            val param = mainAppBarLayout.layoutParams
            param.height = 0
            mainAppBarLayout.layoutParams = param
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraService = CameraXService(cameraExecutor)

        viewFinder.post {
            cameraService.setUpCamera(this, view.context, viewFinder)
        }
        viewModel.addExpensesLiveData.observe(viewLifecycleOwner) { (expenses, _, _) ->
            expenses.imageUri?.let {
                if (it.isNotBlank()) findNavController().navigate(R.id.next_action)
            }

        }

        camera_capture_button.setOnClickListener {
            viewModel.createJPGFile()?.let {
                cameraService.takePhoto(it) { uriPhoto ->
                    viewModel.setLastUriPhoto(uriPhoto)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        with(requireActivity() as AppCompatActivity) {
            window.decorView.systemUiVisibility = View.VISIBLE
            supportActionBar?.show()
            val param = mainAppBarLayout.layoutParams
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT
            mainAppBarLayout.layoutParams = param
        }
    }


}