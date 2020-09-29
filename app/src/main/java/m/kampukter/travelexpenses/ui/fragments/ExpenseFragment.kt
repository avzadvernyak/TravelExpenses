package m.kampukter.travelexpenses.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expense_choice_fragment.recyclerView
import kotlinx.android.synthetic.main.expense_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpenseDeletionResult
import m.kampukter.travelexpenses.ui.ExpenseEditAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ExpenseFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var expenseAdapter: ExpenseEditAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expense_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navController = findNavController()
        //viewModel.deleteExpenseName(null)

        expenseAdapter = ExpenseEditAdapter()
        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = expenseAdapter
        }
        viewModel.expenseList.observe(viewLifecycleOwner, Observer { expenseList ->
            expenseAdapter.setItems(expenseList)
        })
        expenseAdapter.onClickCallback = { expense ->

            viewModel.setQueryExpense(expense.name)
            navController.navigate(R.id.toEditExpenseDialogFragment)
        }
        expenseAdapter.onLongClickCallback = { expense ->
            viewModel.deleteExpenseName(expense.name)
            val bundle = bundleOf("expensePhaseOne" to expense.name)
            navController.navigate(R.id.toDelExpensePhaseOneDialogFragment, bundle)
            true
        }
        viewModel.expenseUpdateMediator.observe(viewLifecycleOwner, Observer {
            Snackbar.make(view, getString(R.string.expense_update_message), Snackbar.LENGTH_SHORT)
                .show()
        })

        /*viewModel.expenseDeletionResultLiveData.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ExpenseDeletionResult.Warning -> {
                    *//* val bundle = bundleOf("expensePhaseSecond" to expense.name)
           navController.navigate(R.id.toDelExpensePhaseOneDialogFragment, bundle)*//*
                    MaterialAlertDialogBuilder(view.context)
                        .setTitle(resources.getString(R.string.delete_record_title))
                        .setMessage(
                            resources.getString(
                                R.string.expense_del_warning,
                                result.countRecords.toString()
                            )
                        )
                        .setPositiveButton(resources.getString(R.string.dialog_yes)) { dialog, _ ->
                            viewModel.deleteExpense(result.expenseName, true)
                            dialog.dismiss()
                        }
                        .show()

                }
                is ExpenseDeletionResult.Success -> Snackbar.make(
                    view,
                    getString(R.string.expense_del_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })*/
        viewModel.expenseDeletionResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is ExpenseDeletionResult.Warning -> {
                    val bundle = bundleOf(
                        "expensePhaseSecond" to resources.getString(
                            R.string.expense_del_warning,
                            result.expenseName,
                            result.countRecords
                        )
                    )
                    navController.navigate(R.id.toDelExpensePhaseSecondDialogFragment, bundle)
                }
                is ExpenseDeletionResult.Success -> Snackbar.make(
                    view,
                    "Success",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        addExpenseFab.setOnClickListener {
            navController.navigate(R.id.toAddExpenseDialogFragment)
        }
    }
}