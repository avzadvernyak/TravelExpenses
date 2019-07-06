package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R

class ExpenseDelDialog : DialogFragment() {

    private var callback: ((Long, Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val expensesId = arguments?.getLong(ARG_EXPENSES_ID)
        val builder = AlertDialog.Builder(activity)

        builder.setTitle(getString(R.string.expense_del_title))
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expensesId?.let { callback?.invoke(expensesId, false) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }
    fun setCallback(callback: (Long, Boolean) -> Unit): ExpenseDelDialog {
        this.callback = callback
        return this
    }
    companion object {
        private const val ARG_EXPENSES_ID = "ARG_EXPENSES_ID"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        const val TAG = "ExpenseDelDialog"
        fun create(expensesId: Long, message: String): ExpenseDelDialog = ExpenseDelDialog().apply {
            arguments = Bundle().apply {
                putLong(ARG_EXPENSES_ID, expensesId)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}