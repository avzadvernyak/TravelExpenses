package m.kampukter.travelexpenses.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expense

typealias ExpenseClickListener<T> = (T) -> Unit

class ExpenseChoiceAdapter(
    private var expenseClickListener: ExpenseClickListener<Expense>? = null
) : RecyclerView.Adapter<ExpenseViewHolder>() {

    private var expense: List<Expense>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        return ExpenseViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expense_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return expense?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        expense?.get(position)?.let { item ->
            holder.bind(item, expenseClickListener)
        }
    }
    fun setList(list: List<Expense>) {
        this.expense = list
        notifyDataSetChanged()
    }
    fun setCallback(callback: ExpenseClickListener<Expense>? ) {
        this.expenseClickListener = callback

    }
}