package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_sum_item.view.*
import m.kampukter.travelexpenses.data.ExpensesSumView

class ExpensesSumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(expensesSum: ExpensesSumView ) {
        with(itemView) {
            expensesSumTextView.text = expensesSum.sum.toString()
            expenseNameTextView.text = expensesSum.expenseName
        }
    }
}