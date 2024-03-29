package m.kampukter.travelexpenses.ui.expenses

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.camerax_fragment.*
import m.kampukter.travelexpenses.CameraXService
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.EditedExpensesField
import m.kampukter.travelexpenses.ui.permissionsForCamera
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePhotoForEditFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.camerax_fragment, container, false)

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
        val cameraService = CameraXService(cameraExecutor)
        viewFinder.post {
            cameraService.setUpCamera(this, view.context, viewFinder)
        }

        viewModel.expensesEdit.observe(viewLifecycleOwner){ (expenses,_)->
            if (expenses.imageUri != null) findNavController().navigate(R.id.next_action)
            camera_capture_button.setOnClickListener {
                viewModel.createJPGFile()?.let {
                    cameraService.takePhoto(it) { uriString ->
                        viewModel.updateExpenses( EditedExpensesField.ImageUriField(expenses.id, uriString ))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}