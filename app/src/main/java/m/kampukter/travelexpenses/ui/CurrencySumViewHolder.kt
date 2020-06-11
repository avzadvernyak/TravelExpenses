package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cyrrency_sum_item.view.*
import m.kampukter.travelexpenses.data.ReportSumView

class CurrencySumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(currencySum: ReportSumView) {
        with(itemView) {
            currencySumTextView.text = currencySum.sum.toString()
            currencyNameTextView.text = currencySum.name
        }
    }
}