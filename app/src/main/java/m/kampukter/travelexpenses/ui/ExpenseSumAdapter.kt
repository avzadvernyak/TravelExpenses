package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ReportSumView

class ExpenseSumAdapter: RecyclerView.Adapter<ExpenseSumViewHolder>() {

    private var expenseSum: List<ReportSumView>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseSumViewHolder {
        return ExpenseSumViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expenses_sum_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return expenseSum?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExpenseSumViewHolder, position: Int) {
        expenseSum?.get(position)?.let { item ->
            holder.bind(item)
        }
    }

    fun setList(list: List<ReportSumView>) {
        this.expenseSum = list
        notifyDataSetChanged()
    }
}
