package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expense

class ExpenseEditAdapter : RecyclerView.Adapter<ExpenseEditViewHolder>() {

    private var items = emptyList<Expense>()

    var onClickCallback: ((Expense) -> Unit)? = null
    var onLongClickCallback: ((Expense) -> Boolean)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseEditViewHolder =
        ExpenseEditViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.expense_edit_list_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ExpenseEditViewHolder, position: Int) {
        holder.bind(items[position], onClickCallback, onLongClickCallback)
    }

    fun setItems(newItems: List<Expense>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition].id == newItems[newItemPosition].id

            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                items[oldItemPosition] == newItems[newItemPosition]

        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }
}