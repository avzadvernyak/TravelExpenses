package m.kampukter.travelexpenses.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.expense_choice_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.ui.ExpenseChoiceAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChoiceExpenseFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var expenseAdapter: ExpenseChoiceAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choice_expense_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        viewModel.expensesById.observe(viewLifecycleOwner, Observer { expenses ->
            expenseAdapter = ExpenseChoiceAdapter { item ->
                viewModel.addExpenses(expenses.copy(expense = item.name))
                findNavController().navigate(R.id.next_action)
            }
            with(recyclerView) {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    context,
                    androidx.recyclerview.widget.RecyclerView.VERTICAL,
                    false
                )
                adapter = expenseAdapter
            }
        })

        viewModel.expenseList.observe(viewLifecycleOwner, Observer { list ->
            expenseAdapter?.setList(list)
        })
    }
}