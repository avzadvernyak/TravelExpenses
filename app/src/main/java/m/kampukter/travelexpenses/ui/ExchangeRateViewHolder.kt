package m.kampukter.travelexpenses.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.exchange_rate_item.view.*
import m.kampukter.travelexpenses.data.CurrentExchangeRate
import java.text.DecimalFormat

class ExchangeRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(currentExchangeRate: CurrentExchangeRate) {
        with(itemView) {
            currencyCodeTextView.text = currentExchangeRate.currencyCode
            currencyNameTextView.text = currentExchangeRate.currencyName

            rateTextView.text = DecimalFormat("####0.0000").format(currentExchangeRate.rate)
        }
    }

}
