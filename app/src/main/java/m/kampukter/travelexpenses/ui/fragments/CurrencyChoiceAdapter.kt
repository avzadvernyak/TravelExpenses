package m.kampukter.travelexpenses.ui.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.CurrencyTable

typealias CurrencyClickListener<T> = (T) -> Unit

class CurrencyChoiceAdapter(
    private val currencyClickListener: CurrencyClickListener<CurrencyTable>? = null
) : RecyclerView.Adapter<CurrencyViewHolder>() {

    private var currency: List<CurrencyTable>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        return CurrencyViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.currency_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return currency?.size ?: 0
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        currency?.get(position)?.let { item ->
            holder.bind(item, currencyClickListener)
        }
    }

    fun setList(list: List<CurrencyTable>) {
        this.currency = list
        notifyDataSetChanged()
    }
}