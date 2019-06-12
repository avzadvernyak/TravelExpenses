package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_item.view.*
import m.kampukter.travelexpenses.data.TravelExpensesView

class ExpensesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(result: TravelExpensesView, expensesClickListener: ExpensesClickListener<TravelExpensesView>?) {

        with(itemView) {
            expensesIdTextView.text = result.id.toString()
            setOnClickListener { expensesClickListener?.invoke(result) }
        }
    }
}