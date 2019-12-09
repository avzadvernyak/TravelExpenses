package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.expenses_sum_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ExpensesSumFragment: Fragment() {
    private val viewModel by viewModel<MyViewModel>()
    private var expensesSumAdapter: ExpensesSumAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.expenses_sum_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.expenses_sum_title)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        expensesSumAdapter = ExpensesSumAdapter()

        with(expensesSumRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = expensesSumAdapter
        }

       viewModel.getExpensesSun().observe(this, Observer { list ->
           expensesSumAdapter?.setList(list)
       })


    }
}