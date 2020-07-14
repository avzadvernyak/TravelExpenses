package m.kampukter.travelexpenses.ui

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rate_item.view.*
import m.kampukter.travelexpenses.data.RateCurrency

class RateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(rateCurrency:  RateCurrency) {
        with(itemView) {
            currencyTextView.text = rateCurrency.name
            dateTextView.text = DateFormat.format("dd/MM/yyyy", rateCurrency.exchangeDate)
            rateTextView.text = rateCurrency.rate.toString()
        }
    }
}
