package m.kampukter.travelexpenses.ui.sum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sum_viewing_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SumCurrencyFragment: Fragment() {
    private val viewModel by sharedViewModel<MyViewModel>()
    private var expensesSumAdapter: ExpenseSumAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sum_viewing_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        expensesSumAdapter = ExpenseSumAdapter()

        with(expensesSumRecyclerView) {
            layoutManager = LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = expensesSumAdapter
        }

        viewModel.getCurrencySum().observe(viewLifecycleOwner, { list ->
            expensesSumAdapter?.setList(list)
        })

    }
}