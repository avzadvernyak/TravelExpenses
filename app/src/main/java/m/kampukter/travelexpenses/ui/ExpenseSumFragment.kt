package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.expenses_sum_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class ExpenseSumFragment: Fragment() {
    private val viewModel by viewModel<MyViewModel>()
    private var expensesSumAdapter: ExpenseSumAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.expenses_sum_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        expensesSumAdapter = ExpenseSumAdapter()

        with(expensesSumRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = expensesSumAdapter
        }

       viewModel.getExpensesSum().observe(this, Observer { list ->
           expensesSumAdapter?.setList(list)
       })


    }
    companion object {
        fun newInstance(): ExpenseSumFragment {
            return ExpenseSumFragment()
        }
    }
}