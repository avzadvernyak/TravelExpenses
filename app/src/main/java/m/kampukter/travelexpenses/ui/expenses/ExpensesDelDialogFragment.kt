package m.kampukter.travelexpenses.ui.expenses

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExpensesDelDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ids = arguments?.getLongArray("Ids")
        return MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.dialog_del_title))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                ids?.let { viewModel.deleteSelectedExpenses( it.toSet())}
                dialog.dismiss()
            }
            .setMessage(getString(R.string.dialog_expenses_del_supporting_text,ids?.size))

            .create()

    }
}