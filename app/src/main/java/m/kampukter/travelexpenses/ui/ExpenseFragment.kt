package m.kampukter.travelexpenses.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.expense_choice_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExpenseFragment: Fragment() {

    private val viewModel by viewModel<MyViewModel>()
    private var expenseAdapter: ExpenseChoiceAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.expense_choice_fragment, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.expense_frag_title)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        expenseAdapter = ExpenseChoiceAdapter { item ->
            activity?.run {
                setResult(AppCompatActivity.RESULT_OK, Intent().putExtra(EXTRA_EXPENSE_ID, item.id.toString()))
                finish()
            }
        }
        with(recyclerView) {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                androidx.recyclerview.widget.RecyclerView.VERTICAL,
                false
            )
            adapter = expenseAdapter
        }
        viewModel.expenseList.observe(this, Observer { list ->
            expenseAdapter?.setList(list)
        })
    }
    companion object {
        const val EXTRA_EXPENSE_ID = "EXTRA_EXPENSE_ID"
    }
}