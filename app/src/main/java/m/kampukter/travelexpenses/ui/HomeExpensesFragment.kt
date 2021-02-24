package m.kampukter.travelexpenses.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.ui.expenses.ExpensesAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private lateinit var navController: NavController

    private lateinit var expensesAdapter: ExpensesAdapter

    private var actionMode: ActionMode? = null

    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_move -> {

                }
                R.id.action_delete -> {
                    viewModel.setSelectedExpenses(expensesAdapter.getSelectedItems())
                    navController.navigate(R.id.toDelExpensesDialogFragment)
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
            expensesAdapter.endSelection()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        navController = findNavController()

        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)

        toolbar?.title = "Поиск в расходах"
        toolbar?.setOnClickListener { navController.navigate(R.id.toSearchExpensesFragment) }

        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        expensesAdapter =
            ExpensesAdapter(view.context) { item ->
                viewModel.setQueryExpensesId(item.id)
                navController.navigate(R.id.toEditExpensesFragment)
            }.apply {
                enableActionMode(actionModeCallback) { count ->
                    actionMode?.title = getString(R.string.expenses_am_title_count, count)
                    if (count == 0) {
                        actionMode?.finish()
                        expensesAdapter.endSelection()
                    }
                }
                viewModel.selectedExpensesLiveData.observe(viewLifecycleOwner, {
                    expensesAdapter.setSelection(it)
                })
            }
        with(recyclerViewExpenses) {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = expensesAdapter
        }
        var nameCurrentFolder: String? = null
        viewModel.currentFolder.observe(viewLifecycleOwner, {
            nameCurrentFolder = it.shortName
        })

        viewModel.expensesInFolder.observe(viewLifecycleOwner, {
            val expenses = mutableListOf<ExpensesMainCollection>()

            if (nameCurrentFolder != null) nameCurrentFolder?.let { name ->
                expenses.add(ExpensesMainCollection.Header(name))
            } else expenses.add(ExpensesMainCollection.Header(resources.getString(R.string.menu_expenses)))
            it.forEach { item -> expenses.add(ExpensesMainCollection.Row(item)) }
            expensesAdapter.setList(expenses)

        })
        viewModel.expensesDeleteStatusMediatorLiveData.observe(viewLifecycleOwner, {
            actionMode?.finish()
            expensesAdapter.endSelection()
            Snackbar.make(
                view,
                getString(R.string.dialog_expenses_del_record),
                Snackbar.LENGTH_SHORT
            )
                .show()
        })

        val addExpensesExtendedFab =
            activity?.findViewById<ExtendedFloatingActionButton>(R.id.addExpensesExtendedFab)
        addExpensesExtendedFab?.let {
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
                    if (dy != 0 && addExpensesExtendedFab.isExtended) {
                        addExpensesExtendedFab.shrink()
                    }
                    super.onScrolled(recyclerView, dx + 16, dy + 16)
                }
            })
            addExpensesExtendedFab.setOnClickListener {
                viewModel.setBufferExpenses(null)
                navController.navigate(R.id.toAddExpensesFragment)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.setSelectedExpenses(expensesAdapter.getSelectedItems())
    }
}
