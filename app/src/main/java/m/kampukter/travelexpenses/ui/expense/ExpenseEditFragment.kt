package m.kampukter.travelexpenses.ui.expense

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.expense_edit_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExpenseEditFragment: Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expense_edit_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.editExpenseLiveData.observe(viewLifecycleOwner) { (expense, list)->
            if (expenseTextInputEdit.text.toString() != expense.name) expenseTextInputEdit.setText(expense.name)
            expensesCount.text = getString(R.string.expense_info_warning, list.size, list.groupBy { it.folderId }.size)

            deleteExpenseMaterialButton.setOnClickListener{
                viewModel.deleteExpense( expense.id )
                findNavController().navigateUp()
            }
        }
        expenseTextInputEdit.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    viewModel.setEditExpenseName( p0.toString() )
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

    }
}