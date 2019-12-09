package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.expenses_fragment.*
import kotlinx.android.synthetic.main.expenses_fragment.toolbar
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.TravelExpensesView
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class TravelExpensesFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var expensesAdapter: ExpensesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.travel_expenses_title)
        }

        val clickEventDelegate: ClickEventDelegate<TravelExpensesView> =
            object : ClickEventDelegate<TravelExpensesView> {
                override fun onClick(item: TravelExpensesView) {
                    (context as AppCompatActivity).startActivity(
                        Intent(
                            context,
                            EditExpensesActivity::class.java
                        ).apply {
                            putExtra(
                                EXTRA_MESSAGE,
                                item.id.toString()
                            )
                        })
                }

                override fun onLongClick(item: TravelExpensesView) {
                    fragmentManager?.let { fm ->
                        val messageStr = "На сумму ${item.sum} \nЗа ${item.expenseName}\n Комментарий -${item.note}"
                        ExpensesDelDialog.create(item.id, messageStr).show(fm, "delDialog")
                    }
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

        viewModel.expenses.observe(this,
            Observer { list ->
                expensesAdapter?.setList(list)
            }
        )
        viewModel.expensesCSVForExport.observe(this, Observer {expensesCSV->
            if (expensesCSV.isNullOrEmpty()) {
                Snackbar.make(
                    recyclerView,
                    "Нет данных для экспорта",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, expensesCSV)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "My Send"))
            }
        })


        addCustomerButton.setOnClickListener { startActivity(Intent(activity, NewExpensesActivity::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.travel_expense_toolbar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            R.id.action_edit_expense -> {
                Log.d("blablabla", "menu ")
                startActivity(Intent(activity, EditExpenseActivity::class.java))
                true
            }
            R.id.action_export -> {
                viewModel.getExpensesCSV(true)
                true
            }
            R.id.action_sumAllExpenses -> {
                startActivity(Intent(activity, ExpensesSumActivity::class.java))
                true
            }
            R.id.action_delAllExpenses -> {
                fragmentManager?.let { fm ->
                    ExpensesDelAllDialog.create().show(fm, ExpensesDelAllDialog.TAG)
                }
                true
            }
            R.id.action_about ->{
                fragmentManager?.let { fm ->
                    AboutDialog.create()
                        .show(fm, AboutDialog.TAG)
                }
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        })
    }

    companion object {
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
    }
}