package m.kampukter.travelexpenses.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.ui.ClickEventDelegate

const val TYPE_HEADER_ALL: Int = 0
const val TYPE_HEADER_SEARCH_RESULT: Int = 1
private const val TYPE_LIST: Int = 2

class ExpensesAdapter(private val clickEventDelegate: ClickEventDelegate<ExpensesWithRate>, private val typeHeader: Int ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expenses = emptyList<ExpensesWithRate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TYPE_HEADER_ALL -> ExpensesHeaderViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_title_item, parent, false)
            )
            TYPE_HEADER_SEARCH_RESULT -> ExpensesHeaderSearchResultViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_title_search_result_item, parent, false)
            )
            else -> ExpensesViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_item, parent, false),
                clickEventDelegate
            )
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ExpensesHeaderSearchResultViewHolder -> holder.bind()
            is ExpensesHeaderViewHolder -> holder.bind()
            is ExpensesViewHolder ->  expenses[position-1].let { item -> holder.bind(item) }
        }

    }

    override fun getItemCount(): Int = expenses.size

    override fun getItemViewType(position: Int): Int = if (position == 0) typeHeader else TYPE_LIST

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
    }
}