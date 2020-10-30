package m.kampukter.travelexpenses.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.add_expenses_fragment.*
import kotlinx.android.synthetic.main.sum_fragment.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddExpensesFragment : Fragment() {


    private val viewModel by sharedViewModel<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.add_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AddMainExpensesFragment()
                    else -> AddPlusExpensesFragment()
                }
            }
        }
        TabLayoutMediator(tab_layout, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Информация"
                else -> "Вложения"
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.add_expenses, menu)

        viewModel.isSavingAllowed.observe(viewLifecycleOwner, { _isSavingAllowed ->
            _isSavingAllowed?.let { menu.findItem(R.id.saveExpenses).isEnabled = it }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveExpenses) {

            viewModel.saveNewExpenses()
            //сброс временной переменной
            viewModel.setBufferExpenses(null)

            hideSystemKeyboard()
            findNavController().navigate(R.id.next_action)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun hideSystemKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

}
