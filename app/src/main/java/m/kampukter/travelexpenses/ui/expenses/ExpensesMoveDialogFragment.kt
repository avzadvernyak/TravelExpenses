package m.kampukter.travelexpenses.ui.expenses

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExpensesMoveDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var items = arrayOf<String>()

        viewModel.folderCandidates.value?.let { list ->
            items = list.map { it.shortName }.toTypedArray()
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Переместить в")
            .setItems(items) { dialog, which ->
                //viewModel.expensesMoveTrigger(items[which])
            }
            .create()

    }
}