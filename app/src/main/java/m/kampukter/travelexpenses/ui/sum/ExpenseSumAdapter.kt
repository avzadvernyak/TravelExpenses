package m.kampukter.travelexpenses.ui.sum

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ReportSumView

class ExpenseSumAdapter: RecyclerView.Adapter<ExpenseSumViewHolder>() {

    private var expenseSum  = listOf<ReportSumView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseSumViewHolder {
        return ExpenseSumViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expenses_sum_item, parent, false)
        )
    }

    override fun getItemCount(): Int = expenseSum.size

    override fun onBindViewHolder(holder: ExpenseSumViewHolder, position: Int) {
            holder.bind(expenseSum[position])
    }

    fun setList(list: List<ReportSumView>) {
        this.expenseSum = list
        notifyDataSetChanged()
    }
}
