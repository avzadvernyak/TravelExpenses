package m.kampukter.travelexpenses.ui.gallery

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelPhotoFromGalleryDialogFragment : DialogFragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_del_photo_gallery))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                viewModel.expenseMediatorLiveData.observe(this,{
                    it.first?.let { expenses ->
                        viewModel.addExpenses(expenses.copy(imageUri = null))
                        findNavController().navigate(R.id.next_action)
                    }
                })

            }
            .create()
}