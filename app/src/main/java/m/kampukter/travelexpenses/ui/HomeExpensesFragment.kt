package m.kampukter.travelexpenses.ui

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.expenses.ExpensesAdapter
import m.kampukter.travelexpenses.ui.expenses.TYPE_HEADER_ALL
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
        //return inflater.inflate(R.layout.home_expenses_fragment, container, false)
        //return inflater.inflate(R.layout.home_fragment, container, false)

        return inflater.inflate(R.layout.expenses_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navController = findNavController()

        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.title = "Поиск в расходах"
        toolbar?.setOnClickListener { navController.navigate(R.id.toSearchExpensesFragment)  }

        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        val clickEventDelegate: ClickEventDelegate<ExpensesWithRate> =
            object : ClickEventDelegate<ExpensesWithRate> {
                override fun onClick(item: ExpensesWithRate) {
                    viewModel.setQueryExpensesId(item.id)
                    navController.navigate(R.id.toEditExpensesFragment)
                }

                override fun onLongClick(item: ExpensesWithRate) {
                    viewModel.setQueryExpensesId(item.id)
                    val arg = resources.getString(
                        R.string.dialog_expenses_del_supporting_text,
                        DateFormat.format("dd/MM/yyyy HH:mm", item.dateTime).toString(),
                        item.sum, item.currency
                    )
                    val bundle = bundleOf("expenses" to arg)
                    navController.navigate(R.id.toDelExpensesDialogFragment, bundle)
                }
            }
        expensesAdapter = ExpensesAdapter(clickEventDelegate, TYPE_HEADER_ALL)
        with(recyclerViewExpenses) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = expensesAdapter
        }
        viewModel.expensesWithRate.observe(viewLifecycleOwner, {
            expensesAdapter.setList(it)
        })
        viewModel.expensesDeleteStatusMediatorLiveData.observe(viewLifecycleOwner, {
            if (!it) Snackbar.make(
                view,
                getString(R.string.dialog_expenses_del_record),
                Snackbar.LENGTH_SHORT
            )
                .show()
        })
        recyclerViewExpenses.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && !addExpensesExtendedFab.isExtended
                    && recyclerView.computeVerticalScrollOffset() == 0
                ) {
                    addExpensesExtendedFab.extend()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d("blabla", "$dx - $dy")
                if (dy != 0 && addExpensesExtendedFab.isExtended) {
                    addExpensesExtendedFab.shrink()
                }
                super.onScrolled(recyclerView, dx + 16, dy+16)
            }
        })
        addExpensesExtendedFab.setOnClickListener { navController.navigate(R.id.toAddExpensesFragment) }
    }
}
