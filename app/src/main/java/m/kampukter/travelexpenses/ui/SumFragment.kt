package m.kampukter.travelexpenses.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.sum_fragment.*
import m.kampukter.travelexpenses.R

class SumFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sum_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> SumCurrencyFragment()
                    else -> SumExpenseFragment()
                }
            }
        }
        TabLayoutMediator(tab_layout, pager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.nav_label_sum_currency)
                else -> getString(R.string.nav_label_sum_expense)
            }
        }.attach()
    }
}