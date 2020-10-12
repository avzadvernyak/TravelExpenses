package m.kampukter.travelexpenses.ui

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LocationPermissionsDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context)
            .setTitle("Получение разрешений")
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    permissionsForLocation,
                    1
                )
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.dialog_cancel)) { dialog, _ ->
                viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                dialog.dismiss()
            }
            .setMessage("А давайте получим разрешения")
            .create()

    }*/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let { context ->
            return MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.dialog_permission_required))
                .setMessage(resources.getString(R.string.dialog_location_access_required))
                .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        permissionsForLocation,
                        PERMISSION_REQUEST_GPS
                    )
                }
                .setNegativeButton(resources.getString(R.string.dialog_cancel)) { _, _ ->
                    viewModel.setSettingStatusGPS(STATUS_GPS_OFF)
                }
                .create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
}