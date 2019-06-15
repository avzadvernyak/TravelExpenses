package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import m.kampukter.travelexpenses.data.TravelExpensesView
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpensesDelDialog : DialogFragment() {
    private val viewModel by viewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val expensesId = arguments?.getLong(ARG_EXPENSES_ID)
        builder.setTitle("Удалить запись")
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expensesId?.let { viewModel.expensesDelete(it) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }

    companion object {
        private const val ARG_EXPENSES_ID = "ARG_EXPENSES_ID"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        fun create(expensesId: Long, message: String): ExpensesDelDialog = ExpensesDelDialog().apply {
            arguments = Bundle().apply {
                putLong(ARG_EXPENSES_ID, expensesId)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}