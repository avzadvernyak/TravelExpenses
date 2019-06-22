package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expense_edit_list_fragment.*
import kotlinx.android.synthetic.main.expense_edit_list_fragment.toolbar
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpenseDeletionResult
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditExpenseFragment : Fragment() {
    private val viewModel by viewModel<MyViewModel>()

    private var expenseEditAdapter: ExpenseEditAdapter? = null

/*
    private lateinit var viewModel: MyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity?).get(MyViewModel::class.java)
    }
*/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.expense_edit_list_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.edit_expense)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        expenseEditAdapter = ExpenseEditAdapter()
        with(expenseListRecyclerView) {
            adapter = expenseEditAdapter
            layoutManager = LinearLayoutManager(context)
        }
        viewModel.expenseList.observe(this, Observer { list ->
            expenseEditAdapter?.setItems(list)
        })
        viewModel.expenseDeletionResultLiveData.observe(this, Observer { result ->
            when(result){
                is ExpenseDeletionResult.Warning -> {
                    fragmentManager?.let { fm ->
                        val messageStr = "Deleting this expense will impact ${result.countRecords} expenses"
                        ExpenseLinkDelDialog.create(result.expenseId, messageStr)
                            .setCallback { id, isForced -> viewModel.deleteExpense(id, isForced) }
                            .show(fm, ExpenseLinkDelDialog.TAG)
                    }
                }
                is ExpenseDeletionResult.Success -> Snackbar.make(expenseListRecyclerView, "Expense was deleted", Snackbar.LENGTH_SHORT).show()
            }
        })
        expenseEditAdapter?.onLongClickCallback = { expense ->
            viewModel.deleteExpense(expense.id, false)
            true
        }

        addExpenseButton.setOnClickListener {
            fragmentManager?.let { fm ->
                NewExpenseDialogFragment.create().show(fm, "newExpenseDialog")
            }
        }
    }

    companion object {
        fun create(): EditExpenseFragment = EditExpenseFragment()
    }
}