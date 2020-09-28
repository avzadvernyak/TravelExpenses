package m.kampukter.travelexpenses.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.del_expenses_dialog_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DelExpensesDialogFragment : DialogFragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    lateinit var materialAlertDialog: MaterialAlertDialogBuilder


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        viewModel.expensesById.observe(this, Observer { expenses ->
            Log.d("blabla", "observe")
            materialAlertDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.expenses_del_title))
                .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                    viewModel.expensesDeleteTrigger(true)
                    dialog.dismiss()
                }
                .setMessage(
                    resources.getString(
                        R.string.expenses_del_supporting_text,
                        expenses.expense,
                        "${expenses.sum}${expenses.currency}",
                        expenses.note
                    )
                )

        })

        return materialAlertDialog.create()
    }

    /* override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.del_expenses_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.expensesById.observe(this, Observer { expenses ->
            expensesDelSupportingTextView.text = getString(
                R.string.expenses_del_supporting_text,
                expenses.expense,
                "${expenses.sum} ${expenses.currency}",
                expenses.note
            )
            notDeleteMaterialButton.setOnClickListener { dismiss() }
            deleteMaterialButton.setOnClickListener {
                viewModel.expensesDelete(expenses.id)
                dismiss()
            }
        })

    }*/
}