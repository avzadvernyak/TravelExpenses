package m.kampukter.travelexpenses.ui.expense

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddExpenseDialogFragment : DialogFragment() {

    private lateinit var materialAlertDialog: MaterialAlertDialogBuilder
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.new_expense_edit_field)
            .setTitle(resources.getString(R.string.add_record_title))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                val editField =
                    (dialog as? AlertDialog)?.findViewById<TextInputEditText>(R.id.newExpenseTextInputEdit)
                if (editField != null) {
                    if (!editField.text.isNullOrBlank()) viewModel.addExpense(
                        Expense(editField.text.toString())
                    )
                }
                dialog.dismiss()
            }

        return materialAlertDialog.create()

    }
}