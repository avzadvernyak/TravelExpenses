package m.kampukter.travelexpenses.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpenseDelDialog : DialogFragment() {
    private val viewModel by viewModel<MyViewModel>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val expensesId = arguments?.getLong(ARG_EXPENSES_ID)

        /*
        viewModel.expenseDeletionResultLiveData.observe(this, Observer {
            when (it) {
                is ExpenseDeletionResult.Warning -> {
                    Log.d("blablabla", "ExpenseDelDialog Warning ${it.countRecords}")
                    fragmentManager?.let { fm ->
                        val messageStr = "Найдены связанные с этой статьей \n записи в количестве ${it.countRecords} шт."
                        expensesId?.let { it1 ->
                            ExpenseLinkDelDialog.create(it1, messageStr).show(fm, ExpenseLinkDelDialog.TAG)
                        }
                    }
                }
                is ExpenseDeletionResult.Success -> Log.d("blablabla", "ExpenseDelDialog Success")
            }
        })
*/
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Удалить запись")
            .setMessage(arguments?.getString(ARG_MESSAGE))
            .setPositiveButton(android.R.string.yes) { _, _ ->
                expensesId?.let { viewModel.delExpense(expensesId) }
            }
            .setNegativeButton(android.R.string.no) { _, _ -> }

        return builder.create()
    }

    companion object {
        private const val ARG_EXPENSES_ID = "ARG_EXPENSES_ID"
        private const val ARG_MESSAGE = "ARG_MESSAGE"
        const val TAG = "ExpenseDelDialog"
        fun create(expensesId: Long, message: String): ExpenseDelDialog = ExpenseDelDialog().apply {
            arguments = Bundle().apply {
                putLong(ARG_EXPENSES_ID, expensesId)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}