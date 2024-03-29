package m.kampukter.travelexpenses.ui.expense

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expense_edit_list_item.view.*
import m.kampukter.travelexpenses.data.Expense

class ExpenseEditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(expense: Expense, onClickCallback: ((Expense) -> Unit)?) {
        with(itemView) {
            expenseIdTextView.text = expense.name

            setOnClickListener { onClickCallback?.invoke(expense) }
        }
    }

}