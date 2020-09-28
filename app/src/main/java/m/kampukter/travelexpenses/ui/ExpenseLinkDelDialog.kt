package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R

class ExpenseLinkDelDialog : DialogFragment() {

    private var callback: ((String, Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val expense = arguments?.getString(ARG_EXPENSE)
        builder.setTitle(getString(R.string.delete_record_title))
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expense?.let { callback?.invoke(expense, true) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }

    fun setCallback(callback: (String, Boolean) -> Unit): ExpenseLinkDelDialog {
        this.callback = callback
        return this
    }

    companion object {
        private const val ARG_EXPENSE = "ARG_EXPENSE"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        const val TAG = "ExpenseLinkDelDialog"
        fun create(expense: String, message: String): ExpenseLinkDelDialog = ExpenseLinkDelDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_EXPENSE, expense)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}