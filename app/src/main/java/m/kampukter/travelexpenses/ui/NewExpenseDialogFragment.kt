package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewExpenseDialogFragment: DialogFragment() {
    private val viewModel by viewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        val builder = AlertDialog.Builder(activity)
        builder.setView(input)
            .setTitle(getString(R.string.new_expense_title))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                viewModel.addExpense(Expense(name = input.text.toString()))
            }
            .setNegativeButton(android.R.string.no){_,_->}

        return builder.create()
    }
    companion object {
        fun create():NewExpenseDialogFragment = NewExpenseDialogFragment()
    }
}