package m.kampukter.travelexpenses.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R

class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.about_title))
            .setMessage(resources.getString(R.string.about_message))
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

    }

}