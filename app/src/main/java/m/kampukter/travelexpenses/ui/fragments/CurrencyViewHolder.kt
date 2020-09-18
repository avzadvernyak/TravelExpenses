package m.kampukter.travelexpenses.ui.fragments

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.currency_item.view.*
import m.kampukter.travelexpenses.data.CurrencyTable

class CurrencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(result: CurrencyTable, currencyClickListener: CurrencyClickListener<CurrencyTable>?) {

        with(itemView) {
            currencyIdTextView.text = result.name
            setOnClickListener { currencyClickListener?.invoke(result) }
        }
    }
}