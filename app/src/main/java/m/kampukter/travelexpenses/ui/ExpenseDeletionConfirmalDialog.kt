package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ExpenseDeletionConfirmalDialog : DialogFragment() {

    private var callback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context)
            .setMessage("Delete this expense?")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                callback?.invoke()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    fun setCallback(callback: () -> Unit): ExpenseDeletionConfirmalDialog {
        this.callback = callback
        return this
    }

}