package m.kampukter.travelexpenses.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R

class HistorySearchExpensesAdapter(private val onClickCallback: ((String) -> Unit)? = null) :
    RecyclerView.Adapter<HistorySearchExpensesViewHolder>() {

    private var historySearchStringList = emptyList<String>()


    override fun getItemCount(): Int = historySearchStringList.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = HistorySearchExpensesViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.history_search_expenses_item, parent, false)
    )

    override fun onBindViewHolder(holder: HistorySearchExpensesViewHolder, position: Int) {
        holder.bind(historySearchStringList[position], onClickCallback)
    }

    fun setList(newList: List<String>) {

        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                historySearchStringList[oldItemPosition] == newList[newItemPosition]

            override fun getOldListSize(): Int = historySearchStringList.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                historySearchStringList[oldItemPosition] == newList[newItemPosition]

        })
        historySearchStringList = newList
        diff.dispatchUpdatesTo(this)
    }
}