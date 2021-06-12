package m.kampukter.travelexpenses.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.android.synthetic.main.expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesExtendedView
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.ui.expenses.ExpensesAdapter
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()

    private lateinit var navController: NavController

    private var expensesAdapter: ExpensesAdapter? = null

    private var actionMode: ActionMode? = null

    private var isInSelection = false

    val countSelectedItem = MutableLiveData<Int>()

    private val expensesClickEventDelegate: ExpensesClickEventDelegate<ExpensesMainCollection> =
        object : ExpensesClickEventDelegate<ExpensesMainCollection> {
            override fun onClick(item: ExpensesMainCollection) {
                if (isInSelection) {
                    countSelectedItem.postValue(expensesAdapter?.toggleItemSelection(item))
                } else {
                    val bundle = bundleOf("expensesId" to item.id)
                    navController.navigate(R.id.toEditExpensesFragment, bundle)
                }
            }

            override fun onLongClick(item: ExpensesMainCollection) {
                expensesAdapter?.let { adapter ->
                    if (!isInSelection) {
                        (context as AppCompatActivity).startSupportActionMode(actionModeCallback)
                        isInSelection = true
                        countSelectedItem.postValue(adapter.toggleItemSelection(item))
                    }
                }
            }

            override fun onLocationClick(item: ExpensesMainCollection) {
                if (isInSelection) {
                    countSelectedItem.postValue(expensesAdapter?.toggleItemSelection(item))
                } else {
                    viewModel.expensesIdEdit(item.id)
                    navController.navigate(R.id.toMapPointFragment)
                }
            }

            override fun onPhotoClick(item: ExpensesMainCollection) {
                if (isInSelection) {
                    countSelectedItem.postValue(expensesAdapter?.toggleItemSelection(item))
                } else {
                    val bundle = bundleOf("galleryItemId" to item.id)
                    navController.navigate(R.id.toGalleryFragment, bundle)
                }
            }
        }

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
                mode?.finish()
            }
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
            isInSelection = false
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

        expensesAdapter = ExpensesAdapter(expensesClickEventDelegate).apply {
            viewModel.savedStateHomeFragmentLiveData.observe(viewLifecycleOwner, {
                if (it.isNotEmpty()) {
                    (context as AppCompatActivity).startSupportActionMode(actionModeCallback)
                    isInSelection = true
                    this.setSelection(it)
                    actionMode?.title = getString(R.string.expenses_am_title_count, it.size)
                }
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

        countSelectedItem.observe(viewLifecycleOwner,
            {
                if (it == 0) {
                    actionMode?.finish()
                    expensesAdapter?.endSelection()
                    isInSelection = false
                } else actionMode?.title = getString(R.string.expenses_am_title_count, it)
            })

        viewModel.expensesInFolder.observe(
            viewLifecycleOwner,
            { (currentFolder, expensesInFolder) ->
                val expenses = mutableListOf<ExpensesMainCollection>()

                expenses.add(ExpensesMainCollection.Header(currentFolder.shortName))
                expensesInFolder.forEach { item -> expenses.add(ExpensesMainCollection.Row(item)) }
                expensesAdapter?.setList(expenses)
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
                navController.navigate(R.id.toAddExpensesFragment)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        expensesAdapter?.let { viewModel.setSavedStateHomeFragment(it.getSelectedItems()) }
    }

    private fun sharedExpenses(expensesList: List<ExpensesExtendedView>) {
        if (expensesList.size == 1) {
            if (expensesList[0].imageUri != null) sharedExpensesImageIntent(expensesList[0])
            else {
                val messageText = getString(
                    R.string.msg_sent_to,
                    expensesList[0].expense,
                    expensesList[0].note,
                    expensesList[0].sum,
                    expensesList[0].currency,
                    DateFormat.format("dd/MM/yyyy HH:mm", expensesList[0].dateTime).toString(),
                    expensesList[0].folderName
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
                    DateFormat.format("dd/MM/yyyy HH:mm", it.dateTime).toString(),
                    expensesList[0].folderName
                )
            }
            sharedExpensesTextIntent(messageText)
        }
    }

    private fun sharedExpensesImageIntent(expenses: ExpensesExtendedView) {
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
                    date,
                    expenses.folderName
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
