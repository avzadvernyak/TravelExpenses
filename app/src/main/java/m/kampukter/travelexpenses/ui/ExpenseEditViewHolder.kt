package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expense_edit_list_item.view.*
import m.kampukter.travelexpenses.data.Expense

class ExpenseEditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(expense: Expense, onClickCallback: ((Expense) -> Unit)?, onLongClickCallback: ((Expense) -> Boolean)?) {
        with(itemView) {
            expenseIdTextView.text = expense.name

            setOnClickListener { onClickCallback?.invoke(expense) }
            setOnLongClickListener { onLongClickCallback?.invoke(expense) ?: false }
        }
    }

}