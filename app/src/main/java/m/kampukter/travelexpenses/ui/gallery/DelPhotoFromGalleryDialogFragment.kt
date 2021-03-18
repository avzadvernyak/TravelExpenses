package m.kampukter.travelexpenses.ui.gallery

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelPhotoFromGalleryDialogFragment : DialogFragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_del_photo_gallery))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                arguments?.getLong("expensesIdDelImage")?.let {
                    viewModel.deleteImageFromExpenses(it)
                    dialog.dismiss()
                }
            }
            .create()
}