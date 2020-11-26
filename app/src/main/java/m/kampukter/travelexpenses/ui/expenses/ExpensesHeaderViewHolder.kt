package m.kampukter.travelexpenses.ui.expenses

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_title_item.view.*
import m.kampukter.travelexpenses.R

class ExpensesHeaderViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind() {
        with(itemView) {
            titleExpensesTextView.text = resources.getString(R.string.nav_label_expenses)
        }
    }
}