package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.RateCurrency

class RateAdapter: RecyclerView.Adapter<RateViewHolder>()  {

    private var rateCurrencyList: List<RateCurrency>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        return RateViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.rate_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return rateCurrencyList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        rateCurrencyList?.get(position)?.let { item ->
            holder.bind(item)
        }
    }

    fun setList(list: List<RateCurrency>) {
        this.rateCurrencyList = list
        notifyDataSetChanged()
    }
}