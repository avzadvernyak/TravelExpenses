package m.kampukter.travelexpenses.ui.expenses

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.search_expenses_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SearchExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private lateinit var historySearchExpensesAdapter: HistorySearchExpensesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.search_expenses_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historySearchExpensesAdapter = HistorySearchExpensesAdapter()
        with(historySearchRecyclerView){
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context,
                RecyclerView.VERTICAL,
                false
            )
            adapter = historySearchExpensesAdapter
        }

        historySearchExpensesAdapter.setList(viewModel.getHistorySearchStringExpenses())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_expenses, menu)

        val searchView = (menu.findItem(R.id.searchExpenses)?.actionView as? SearchView)

        searchView?.isIconified = false
        searchView?.onActionViewExpanded()
/*

        val magId = resources.getIdentifier("android:id/search_mag_icon", null, null)
        searchView?.let {
            val magImage = it.findViewById<View>(magId) as ImageView
            magImage.layoutParams = LinearLayout.LayoutParams(0, 0)
        }
*/


        /*val searchHintIcon = activity?.findViewById<ImageView>(R.id.search_mag_icon)
        searchHintIcon?.visibility = View.INVISIBLE*/


        viewModel.searchStringExpensesLiveData.observe(
            this,
            {
                if (it.isNullOrEmpty()) searchView?.queryHint = "Поиск в расходах"
                else searchView?.setQuery(it, false)
            })


        searchView?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.setSearchStringExpenses(it) }
                    //Log.d("blabla","------onQueryTextSubmit")
                    findNavController().navigate(R.id.toSearchResultExpensesFragment)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrEmpty()) searchView?.queryHint = "Поиск в расходах"
                    return false
                }
            })
    }
}