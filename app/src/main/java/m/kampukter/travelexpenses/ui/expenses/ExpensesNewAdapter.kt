package m.kampukter.travelexpenses.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.ClickEventDelegate

private const val TYPE_HEADER: Int = 2
private const val TYPE_LIST: Int = 1

class ExpensesNewAdapter(private val clickEventDelegate: ClickEventDelegate<ExpensesWithRate>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expenses = emptyList<ExpensesMainCollection>()

    override fun getItemCount(): Int = expenses.size

    override fun getItemViewType(position: Int): Int = when (expenses[position]) {
        is ExpensesMainCollection.Header -> TYPE_HEADER
        is ExpensesMainCollection.Row -> TYPE_LIST
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_HEADER -> ExpensesHeaderNewViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_title_item, parent, false)
            )
            else -> ExpensesNewViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_item, parent, false),
                clickEventDelegate
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ExpensesNewViewHolder -> holder.bind(expenses[position])
            is ExpensesHeaderNewViewHolder -> holder.bind(expenses[position])
        }
    }

    fun setList(newListExpenses: List<ExpensesMainCollection>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                expenses[oldItemPosition].id == newListExpenses[newItemPosition].id

            override fun getOldListSize(): Int = expenses.size

            override fun getNewListSize(): Int = newListExpenses.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                expenses[oldItemPosition].contentHash == newListExpenses[newItemPosition].contentHash

        })
        expenses = newListExpenses
        diff.dispatchUpdatesTo(this)
        /*expenses = newListExpenses
        notifyDataSetChanged()*/
    }
}