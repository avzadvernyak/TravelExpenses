package m.kampukter.travelexpenses.ui.expenses

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_title_item.view.*
import m.kampukter.travelexpenses.data.ExpensesMainCollection

class ExpensesHeaderNewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(item: ExpensesMainCollection) {
        with(itemView) {
            val data = (item as ExpensesMainCollection.Header).title
            titleExpensesTextView.text = data
        }
    }

}
