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
import m.kampukter.travelexpenses.NetworkLiveData
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class TravelExpensesFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var expensesAdapter: ExpensesAdapter? = null

    private var myMenu: Menu? = null
    private var defaultProgramCurrency: Int? = null
    private var isNetwork = false

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
        defaultProgramCurrency = mainApplication.getActiveCurrencySession()

        val clickEventDelegate: ClickEventDelegate<ExpensesWithRate> =
            object : ClickEventDelegate<ExpensesWithRate> {
                override fun onClick(item: ExpensesWithRate) {
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

                override fun onLongClick(item: ExpensesWithRate) {
                    fragmentManager?.let { fm ->
                        val messageStr =
                            "На сумму ${item.sum} \nЗа ${item.expense}\n Комментарий -${item.note}"
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
        viewModel.expensesWithRate.observe(viewLifecycleOwner, Observer {
            expensesAdapter?.setList(it)
        })
        viewModel.expensesCSVForExport.observe(viewLifecycleOwner, Observer { expensesCSV ->
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
        NetworkLiveData.observe(viewLifecycleOwner, Observer {
            myMenu?.findItem(R.id.show_rate)?.isVisible = it and (defaultProgramCurrency != null)
            isNetwork = it
        })

        addCustomerButton.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    NewExpensesActivity::class.java
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.travel_expense_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        myMenu = menu
        myMenu?.findItem(R.id.show_rate)?.isVisible = isNetwork and (defaultProgramCurrency != null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when (item.itemId) {
            R.id.action_edit_expense -> {
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
            R.id.action_about -> {

                fragmentManager?.let { fm ->
                    AboutDialog.create()
                        .show(fm, AboutDialog.TAG)
                }
                true
            }
            R.id.settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                true
            }
            R.id.show_rate -> {
                if (defaultProgramCurrency != null) startActivity(
                    Intent(
                        activity,
                        CurrentExchangeRateActivity::class.java
                    )
                )
                true
            }
            R.id.show_archive_rate -> {
                startActivity(Intent(activity, RateActivity::class.java))
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