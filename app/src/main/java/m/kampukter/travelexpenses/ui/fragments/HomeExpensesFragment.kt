package m.kampukter.travelexpenses.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.expenses_fragment.*
import kotlinx.android.synthetic.main.expenses_fragment.recyclerView
import kotlinx.android.synthetic.main.home_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.*
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeExpensesFragment: Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private var expensesAdapter: ExpensesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_expenses_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val clickEventDelegate: ClickEventDelegate<ExpensesWithRate> =
            object : ClickEventDelegate<ExpensesWithRate> {
                override fun onClick(item: ExpensesWithRate) {
                    viewModel.setQueryExpensesId(item.id)

                    navController.navigate(R.id.toEditExpensesFragment)
                }

                override fun onLongClick(item: ExpensesWithRate) {

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
            expensesAdapter?.setList(it)
        })
        addExpensesFab.setOnClickListener { navController.navigate(R.id.toChoiceExpenseFragment ) }
    }
}