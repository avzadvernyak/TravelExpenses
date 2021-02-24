package m.kampukter.travelexpenses.ui.expense

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

open class MaterialDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var dialogView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme).apply {
            dialogView =
                onCreateView(LayoutInflater.from(requireContext()), null, savedInstanceState)
            dialogView?.let { onViewCreated(it, savedInstanceState) }
            setView(dialogView)
        }
            .setPositiveButton(resources.getString(R.string.dialog_save)) { dialog, _ ->
                val editField =
                    (dialog as? AlertDialog)?.findViewById<TextInputEditText>(R.id.newExpenseTextInputEdit)
                val saveField = editField?.text.toString()
                if (!saveField.isBlank()) viewModel.updateExpense(saveField)
                dialog.dismiss()
            }
            .create()
    }

    override fun getView(): View? {
        return dialogView
    }
}
