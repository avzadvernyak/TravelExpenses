package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ReportSumView

class CurrencySumAdapter: RecyclerView.Adapter<CurrencySumViewHolder>() {

    private var currencySum = listOf<ReportSumView>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencySumViewHolder {
        return CurrencySumViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.cyrrency_sum_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return currencySum.size
    }

    override fun onBindViewHolder(holder: CurrencySumViewHolder, position: Int) {
            holder.bind(currencySum[position])
    }

    fun setList(list: List<ReportSumView>) {
        this.currencySum = list
        notifyDataSetChanged()
    }
}
