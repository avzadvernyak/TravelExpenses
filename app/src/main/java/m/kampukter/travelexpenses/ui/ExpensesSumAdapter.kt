package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesSumView

class ExpensesSumAdapter: RecyclerView.Adapter<ExpensesSumViewHolder>() {

    private var expensesSum: List<ExpensesSumView>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesSumViewHolder {
        return ExpensesSumViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expenses_sum_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return expensesSum?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExpensesSumViewHolder, position: Int) {
        expensesSum?.get(position)?.let { item ->
            holder.bind(item)
        }
    }

    fun setList(list: List<ExpensesSumView>) {
        this.expensesSum = list
        notifyDataSetChanged()
    }
}
