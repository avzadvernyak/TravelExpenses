package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R

class ExpenseDelDialog : DialogFragment() {

    private var callback: ((String, Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val expense = arguments?.getString(ARG_MESSAGE)

        builder.setTitle(getString(R.string.expense_del_title))
            .setMessage(expense)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expense?.let { callback?.invoke(expense, false) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }
    fun setCallback(callback: (String, Boolean) -> Unit): ExpenseDelDialog {
        this.callback = callback
        return this
    }
    companion object {
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        const val TAG = "ExpenseDelDialog"
        fun create(message: String): ExpenseDelDialog = ExpenseDelDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_MESSAGE, message)
            }
        }
    }
}