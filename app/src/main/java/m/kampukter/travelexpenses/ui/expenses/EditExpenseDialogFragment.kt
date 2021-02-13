package m.kampukter.travelexpenses.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.new_expense_edit_field.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.ui.MaterialDialogFragment
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

}