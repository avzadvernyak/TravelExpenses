package m.kampukter.travelexpenses.ui.expenses

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.EditedExpensesField
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelAttachmentPhotoDialogFragment : DialogFragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_del_photo))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                viewModel.expensesIdEditLiveData.observe(this) { expensesId ->
                    viewModel.updateExpenses( EditedExpensesField.ImageUriField(expensesId,null))
                    findNavController().navigate(R.id.next_action)
                }
            }
            .create()

}