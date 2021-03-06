package m.kampukter.travelexpenses.ui.expenses

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelAllExpensesDialogFragment: DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialog_expenses_del_all_title))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                viewModel.deleteAllExpenses()
                dialog.dismiss()
            }
            .setMessage(resources.getString(R.string.dialog_expenses_del_all_message))
            .create()

    }
}