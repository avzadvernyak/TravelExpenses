package m.kampukter.travelexpenses.ui.expenses

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_result_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import android.text.format.DateFormat
import m.kampukter.travelexpenses.data.ExpensesMainCollection


class SearchResultExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private lateinit var expensesAdapter: ExpensesAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.setSearchResultExpensesOpenActive(true)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.search_result_expenses_fragment, container, false)
    }

    override fun onStop() {
        viewModel.setSearchResultExpensesOpenActive(false)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
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
        expensesAdapter = ExpensesAdapter(view.context)
        with(resultRecyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = expensesAdapter
        }
        viewModel.expensesSearchResult.observe(viewLifecycleOwner, {
            val expenses = mutableListOf<ExpensesMainCollection>()
            expenses.add(ExpensesMainCollection.Header(resources.getString(R.string.nav_label_search_result_expenses)))
            it.forEach { item -> expenses.add(ExpensesMainCollection.Row(item)) }
            expensesAdapter.setList(expenses)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_expenses, menu)

        val searchView = (menu.findItem(R.id.searchExpenses)?.actionView as? SearchView)

        searchView?.isIconified = false
        searchView?.onActionViewExpanded()
        viewModel.searchStringExpensesLiveData.observe(this, { searchView?.setQuery(it, false) })

        searchView?.doOnLayout {
            searchView.clearFocus()
        }
        searchView?.setOnQueryTextFocusChangeListener { _, isFocused ->
            if (isFocused && viewModel.getSearchResultExpensesOpenActive()) findNavController().navigate(
                R.id.next_action
            )

        }
        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchStringExpenses("")
                    return false
                }

            })
    }
}