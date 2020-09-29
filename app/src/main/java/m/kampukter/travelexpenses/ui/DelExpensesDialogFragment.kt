package m.kampukter.travelexpenses.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelExpensesDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.expenses_del_title))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                viewModel.expensesDeleteTrigger(true)
                dialog.dismiss()
            }
            .setMessage(arguments?.getString("expenses"))
            .create()

    }
}