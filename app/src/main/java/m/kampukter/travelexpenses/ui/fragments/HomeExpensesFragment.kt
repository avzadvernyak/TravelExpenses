package m.kampukter.travelexpenses.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.createGraph
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expenses_fragment.recyclerView
import kotlinx.android.synthetic.main.home_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import m.kampukter.travelexpenses.ui.ExpensesAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeExpensesFragment : Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var expensesAdapter: ExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        val navController = findNavController()
        val clickEventDelegate: ClickEventDelegate<ExpensesWithRate> =
            object : ClickEventDelegate<ExpensesWithRate> {
                override fun onClick(item: ExpensesWithRate) {
                    viewModel.setQueryExpensesId(item.id)
                    navController.navigate(R.id.toEditExpensesFragment)
                }

                override fun onLongClick(item: ExpensesWithRate) {
                    viewModel.setQueryExpensesId(item.id)
                    navController.navigate(R.id.toDelExpensesDialogFragment)
                }
            }
        expensesAdapter = ExpensesAdapter(clickEventDelegate)
        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = expensesAdapter
        }
        viewModel.expensesWithRate.observe(viewLifecycleOwner, Observer {
            expensesAdapter.setList(it)
        })
        viewModel.expensesDeleteStatusMediatorLiveData.observe(viewLifecycleOwner, Observer{
            Snackbar.make(view, getString(R.string.expenses_del_record), Snackbar.LENGTH_SHORT)
                .show()
        })
        addExpensesFab.setOnClickListener { navController.navigate(R.id.toAddExpensesFragment) }
    }
}