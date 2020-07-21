package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.RateCurrency

class RateAdapter: RecyclerView.Adapter<RateViewHolder>()  {

    private var rateCurrencyList= listOf<RateCurrency>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        return RateViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.rate_item, parent, false)
        )
    }

    override fun getItemCount(): Int = rateCurrencyList.size

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
            holder.bind(rateCurrencyList[position])
    }

    fun setList(list: List<RateCurrency>) {
        this.rateCurrencyList = list
        notifyDataSetChanged()
    }
}