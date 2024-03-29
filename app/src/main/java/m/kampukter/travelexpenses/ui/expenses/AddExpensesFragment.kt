package m.kampukter.travelexpenses.ui.expenses

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.add_expenses.*
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.ui.STATUS_GPS_ON
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AddExpensesFragment : Fragment() {

    private val viewModel by sharedViewModel<MyViewModel>()
    private val listFragments: MutableList<Fragment> =
        mutableListOf(
            AddMainExpensesFragment(),
            AddPlusExpensesFragment()
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       /* viewModel.savedSettingsLiveData.observe(viewLifecycleOwner) { settings ->
            if (settings.statusGPS == STATUS_GPS_ON) {

            }
        }
*/
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
        saveExpensesFAB.setOnClickListener {
            // Save expenses
            viewModel.addNewExpenses()

            (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                view.windowToken,
                0
            )
            findNavController().navigate(R.id.next_action)
        }
    }


}
