package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.android.synthetic.main.expenses_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Currency
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.data.MyDatabase
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class TravelExpensesFragment : Fragment() {

    //private val viewModel by viewModel<MyViewModel>()
    private val db = get<MyDatabase>().travelExpensesDao().getAll()
    private var expensesAdapter: ExpensesAdapter? = null

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
            title = "Travel"
        }

        expensesAdapter = ExpensesAdapter()

        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = expensesAdapter
        }
        /*
        viewModel.expenses.observe(this,
            Observer { list -> expensesAdapter?.setList(list) }
        )
        */

        //addCustomerButton.setOnClickListener { startActivity(Intent(activity, AddNewCustomerActivity::class.java)) }
    }
}