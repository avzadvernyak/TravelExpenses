package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_sum_item.view.*
import m.kampukter.travelexpenses.data.ReportSumView

class ExpenseSumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(expensesSum: ReportSumView ) {
        with(itemView) {
            dateTextView.text = expensesSum.sum.toString()
            rateTextView.text = expensesSum.name
            currencyTextView.text = expensesSum.note
        }
    }
}