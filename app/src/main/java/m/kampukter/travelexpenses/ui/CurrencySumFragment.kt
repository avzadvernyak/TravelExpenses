package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.currency_sum_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CurrencySumFragment : Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var currencySumAdapter: CurrencySumAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.currency_sum_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        currencySumAdapter = CurrencySumAdapter()

        with(currencySumRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = currencySumAdapter
        }

        viewModel.getCurrencySum().observe(this, Observer { list ->
            currencySumAdapter?.setList(list)
        })


    }
    companion object {
        fun newInstance(): CurrencySumFragment {
            return CurrencySumFragment()
        }
    }
}
