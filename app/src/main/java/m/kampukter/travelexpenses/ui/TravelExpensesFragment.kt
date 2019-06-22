package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.expenses_fragment.*
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
            title = "Расходы"
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
            Observer { list -> expensesAdapter?.setList(list) }
        )


        addCustomerButton.setOnClickListener { startActivity(Intent(activity, NewExpensesActivity::class.java)) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.travel_expense_toolbar_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return (when(item.itemId) {
            R.id.action_edit_expense -> {
                Log.d("blablabla", "menu ")
                startActivity(Intent(activity, EditExpenseActivity::class.java))
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