package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpenseLinkDelDialog : DialogFragment() {

    private var callback: ((Long, Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val expensesId = arguments?.getLong(ARG_EXPENSES_ID)
        builder.setTitle("Удалить запись")
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expensesId?.let { callback?.invoke(expensesId, true) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }

    fun setCallback(callback: (Long, Boolean) -> Unit): ExpenseLinkDelDialog {
        this.callback = callback
        return this
    }

    companion object {
        private const val ARG_EXPENSES_ID = "ARG_EXPENSES_ID"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        const val TAG = "ExpenseLinkDelDialog"
        fun create(expensesId: Long, message: String): ExpenseLinkDelDialog = ExpenseLinkDelDialog().apply {
            arguments = Bundle().apply {
                putLong(ARG_EXPENSES_ID, expensesId)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}