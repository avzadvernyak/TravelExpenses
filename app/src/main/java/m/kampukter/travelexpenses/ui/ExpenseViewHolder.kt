package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expense_item.view.*
import m.kampukter.travelexpenses.data.Expense

class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(result: Expense, expenseClickListener: ExpenseClickListener<Expense>?) {

        with(itemView) {
            expenseIdTextView.text = result.name
            setOnClickListener { expenseClickListener?.invoke(result) }
        }
    }
}