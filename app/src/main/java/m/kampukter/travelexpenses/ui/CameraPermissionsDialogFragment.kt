package m.kampukter.travelexpenses.ui

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R

class CameraPermissionsDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let { context ->
            return MaterialAlertDialogBuilder(context)
                .setTitle(resources.getString(R.string.dialog_permission_required))
                .setMessage(resources.getString(R.string.dialog_camera_access_required))
                .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        permissionsForCamera,
                        PERMISSION_REQUEST_CAMERA
                    )
                }
                .setNegativeButton(resources.getString(R.string.dialog_cancel)) { _, _ ->
                }
                .create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
}