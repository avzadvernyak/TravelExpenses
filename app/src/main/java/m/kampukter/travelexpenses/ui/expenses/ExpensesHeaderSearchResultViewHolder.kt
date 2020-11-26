package m.kampukter.travelexpenses.ui.expenses

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_title_search_result_item.view.*
import m.kampukter.travelexpenses.R

class ExpensesHeaderSearchResultViewHolder ( itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind() {
        with(itemView) {
            titleSearchResultExpensesTextView.text = resources.getString(R.string.nav_label_search_result_expenses)
        }
    }
}