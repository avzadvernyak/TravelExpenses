package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate

class ExpensesAdapter(private val clickEventDelegate: ClickEventDelegate<ExpensesWithRate>) :
    RecyclerView.Adapter<ExpensesViewHolder>() {
    private var expenses = emptyList<ExpensesWithRate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        return ExpensesViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expenses_item, parent, false),
            clickEventDelegate
        )
    }

    override fun getItemCount(): Int = expenses.size


    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        expenses[position].let { item -> holder.bind(item) }
    }

    fun setList(newListExpenses: List<ExpensesWithRate>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                expenses[oldItemPosition].rate == newListExpenses[newItemPosition].rate

            override fun getOldListSize(): Int = expenses.size

            override fun getNewListSize(): Int = newListExpenses.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                expenses[oldItemPosition] == newListExpenses[newItemPosition]

        })
        expenses = newListExpenses
        diff.dispatchUpdatesTo(this)
        /*this.expenses = list
        notifyDataSetChanged()*/
    }
}