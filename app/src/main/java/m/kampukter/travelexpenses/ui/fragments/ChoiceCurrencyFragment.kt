package m.kampukter.travelexpenses.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expense_choice_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChoiceCurrencyFragment: Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private var currencyAdapter: CurrencyChoiceAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.choice_currency_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        viewModel.expensesById.observe(viewLifecycleOwner, Observer { expenses ->
            currencyAdapter = CurrencyChoiceAdapter { item ->
                viewModel.addExpenses(expenses.copy(currency = item.name))
                findNavController().navigate(R.id.next_action)
            }
            with(recyclerView) {
                layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL,
                    false
                )
                adapter = currencyAdapter
            }
        })

        viewModel.currencyTableList.observe(viewLifecycleOwner, Observer { list ->
            currencyAdapter?.setList(list)
        })
    }
}