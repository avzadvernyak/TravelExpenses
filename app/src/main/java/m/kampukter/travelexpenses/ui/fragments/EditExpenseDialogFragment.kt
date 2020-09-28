package m.kampukter.travelexpenses.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.edit_epense_dialog_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class EditExpenseDialogFragment : MaterialDialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.new_expense_edit_field, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setTitle(resources.getString(R.string.edit_record_title))

        viewModel.expenseById.observe(this, Observer {
            newExpenseTextInputEdit.setText(it.name)
        })
    }
    //private lateinit var materialAlertDialog: MaterialAlertDialogBuilder

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentExpense = arguments?.getString("expenseArg")
        materialAlertDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            val dialogView =
                onCreateView(LayoutInflater.from(requireContext()), null, savedInstanceState)
            dialogView?.let { onViewCreated(it, savedInstanceState) }
            setView(dialogView)
            val editField = dialogView?.findViewById<TextInputEditText>(R.id.newExpenseTextInputEdit)
            editField?.setText("1111111111111111")
        }
            .setView(R.layout.new_expense_edit_field)
            .setTitle(resources.getString(R.string.edit_record_title))
            .setPositiveButton(resources.getString(R.string.dialog_save)) { dialog, _ ->
                val editField =
                    (dialog as? AlertDialog)?.findViewById<TextInputEditText>(R.id.newExpenseTextInputEdit)
                val saveField = editField?.text.toString()
                if (!currentExpense.isNullOrBlank() && !saveField.isNullOrBlank()) {
                    if (currentExpense != saveField) Log.d(
                        "blabla",
                        "Save ${editField?.text.toString()}"
                    )
                }
                dialog.dismiss()
            }
        return materialAlertDialog.create()
    }*/

}