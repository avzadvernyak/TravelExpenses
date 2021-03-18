package m.kampukter.travelexpenses.ui.expense

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expense_fragment.*
import kotlinx.android.synthetic.main.expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpenseDeletionResult
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

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        val navController = findNavController()

        expenseAdapter = ExpenseEditAdapter()
        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = expenseAdapter
        }
        viewModel.expenseList.observe(viewLifecycleOwner, { expenseList ->
            expenseAdapter.setItems(expenseList)
        })
        expenseAdapter.onClickCallback = { expense ->
            viewModel.setEditExpense( expense )
            navController.navigate(R.id.toExpenseEditFragment)
        }
        val addFAB = activity?.findViewById<ExtendedFloatingActionButton>(R.id.addExpenseFab)
        addFAB?.setOnClickListener {
            navController.navigate(R.id.toAddExpenseDialogFragment)
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                addFAB?.let {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && !addFAB.isExtended
                        && recyclerView.computeVerticalScrollOffset() == 0
                    ) {
                        addFAB.extend()
                    }
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                addFAB?.let {
                    if (dy != 0 && addFAB.isExtended) {
                        addFAB.shrink()
                    }
                }
                super.onScrolled(recyclerView, dx + 16, dy + 16)
            }
        })
    }
}