package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpensesDelAllDialog : DialogFragment() {
    private val viewModel by viewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.expenses_del_all_title))
            .setMessage(
                getString(R.string.expenses_del_all_message)
            )
            .setPositiveButton(android.R.string.yes) { _, _ ->
                viewModel.deleteAllExpenses()
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }

    companion object {
        const val TAG = "ExpensesDelAllDialog"
        fun create(): ExpensesDelAllDialog = ExpensesDelAllDialog()
    }
}
