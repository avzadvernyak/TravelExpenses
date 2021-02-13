package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExchangeCurrentRate

class ExchangeRateAdapter : RecyclerView.Adapter<ExchangeRateViewHolder>() {

    private var exchangeRateList = listOf<ExchangeCurrentRate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeRateViewHolder {
        return ExchangeRateViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.exchange_rate_item, parent, false)
        )
    }

    override fun getItemCount(): Int = exchangeRateList.size

    override fun onBindViewHolder(holder: ExchangeRateViewHolder, position: Int) {
            holder.bind(exchangeRateList[position])
    }

    fun setList(list: List<ExchangeCurrentRate>) {
        this.exchangeRateList = list
        notifyDataSetChanged()
    }
}
