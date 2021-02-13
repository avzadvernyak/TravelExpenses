package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.exchange_rate_item.view.*
import m.kampukter.travelexpenses.data.ExchangeCurrentRate
import java.text.DecimalFormat

class ExchangeRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(exchangeCurrentRate: ExchangeCurrentRate) {
        with(itemView) {
            currencyCodeTextView.text = exchangeCurrentRate.currencyCode
            currencyNameTextView.text = exchangeCurrentRate.currencyName

            rateTextView.text = DecimalFormat("####0.0000").format(exchangeCurrentRate.rate)
        }
    }

}
