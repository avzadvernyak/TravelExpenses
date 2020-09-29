package m.kampukter.travelexpenses.ui.expense

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelExpensePhaseOneDialogFragment: DialogFragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.expenses_del_title))
            .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                viewModel.deleteExpenseTrigger(false)
                findNavController().navigate(R.id.toExpenseFragment)
                dialog.dismiss()
            }
            .setMessage(arguments?.getString("expensePhaseOne"))
            .create()

    }
}