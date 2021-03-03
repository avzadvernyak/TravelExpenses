package m.kampukter.travelexpenses.ui.expenses

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_result_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import android.text.format.DateFormat
import androidx.appcompat.view.ActionMode
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import m.kampukter.travelexpenses.data.ExpensesMainCollection


class SearchResultExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private var expensesAdapter: ExpensesAdapter? = null

    private lateinit var navController: NavController
    private var actionMode: ActionMode? = null

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            expensesAdapter?.let { adapter ->
                val list = adapter.getSelectedItems()
                val bundle = bundleOf("Ids" to list.map { (it as ExpensesMainCollection.Row).id }
                    .toLongArray())
                when (item?.itemId) {
                    R.id.action_move -> {
                        navController.navigate(R.id.toExpensesMoveFragment, bundle)
                    }
                    R.id.action_share -> {
                        sharedExpenses(list.map { (it as ExpensesMainCollection.Row).expenses })
                    }
                    R.id.action_delete -> {
                        navController.navigate(R.id.toExpensesDelDialogFragment, bundle)
                    }
                }
            }
            mode?.finish()
            return true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            actionMode = mode
            mode?.menuInflater?.inflate(R.menu.homefragment_actionmode_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            expensesAdapter?.endSelection()
        }
    }

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

        navController = findNavController()

        expensesAdapter =
            ExpensesAdapter(view.context) { item ->
                viewModel.setQueryExpensesId(item.id)
                navController.navigate(R.id.toEditExpensesFragment)
            }.apply {
                enableActionMode(actionModeCallback) { count ->
                    actionMode?.title = getString(R.string.expenses_am_title_count, count)
                    if (count == 0) {
                        actionMode?.finish()
                        expensesAdapter?.endSelection()
                    }
                }
                viewModel.savedStateSearchFragmentLiveData.observe(viewLifecycleOwner, {
                    this.setSelection(it)
                })
            }

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
            expensesAdapter?.setList(expenses)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        expensesAdapter?.let { viewModel.setSavedStateSearchFragment(it.getSelectedItems()) }
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

    private fun sharedExpenses(expensesList: List<ExpensesWithRate>) {
        if (expensesList.size == 1) {
            if (expensesList[0].imageUri != null) sharedExpensesImageIntent(expensesList[0])
            else {
                val messageText = getString(
                    R.string.msg_sent_to,
                    expensesList[0].expense,
                    expensesList[0].note,
                    expensesList[0].sum,
                    expensesList[0].currency,
                    DateFormat.format("dd/MM/yyyy HH:mm", expensesList[0].dateTime).toString()
                )
                sharedExpensesTextIntent(messageText)
            }
        } else {
            var messageText = ""
            expensesList.forEach {
                messageText += getString(
                    R.string.msg_sent_to,
                    it.expense,
                    it.note,
                    it.sum,
                    it.currency,
                    DateFormat.format("dd/MM/yyyy HH:mm", it.dateTime).toString()
                )
            }
            sharedExpensesTextIntent(messageText)
        }
    }

    private fun sharedExpensesImageIntent(expenses: ExpensesWithRate) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            val date =
                DateFormat.format("dd/MM/yyyy HH:mm", expenses.dateTime).toString()
            putExtra(
                Intent.EXTRA_TEXT, getString(
                    R.string.msg_sent_to,
                    expenses.expense,
                    expenses.note,
                    expenses.sum,
                    expenses.currency,
                    date
                )
            )
            putExtra(Intent.EXTRA_STREAM, Uri.parse(expenses.imageUri))
            type = "image/*"
        }
        startActivity(Intent.createChooser(sendIntent, "Share photo"))
    }

    private fun sharedExpensesTextIntent(messageText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, messageText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share expenses"))
    }
}