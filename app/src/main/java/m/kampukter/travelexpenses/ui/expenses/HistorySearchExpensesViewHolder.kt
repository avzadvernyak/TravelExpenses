package m.kampukter.travelexpenses.ui.expenses

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_search_expenses_item.view.*

class HistorySearchExpensesViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind( item: String){
        itemView.searchStringTextView.text = item
    }
}