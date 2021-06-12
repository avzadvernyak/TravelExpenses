package m.kampukter.travelexpenses.ui.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.ui.ExpensesClickEventDelegate

private const val TYPE_HEADER: Int = 2
private const val TYPE_LIST: Int = 1

class ExpensesAdapter(
    private var clickListener: ExpensesClickEventDelegate<ExpensesMainCollection>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expenses = emptyList<ExpensesMainCollection>()

    private var selectedItems = mutableSetOf<ExpensesMainCollection>()

    private var selectionCountListener: ((Int) -> Unit)? = null

    fun toggleItemSelection(item: ExpensesMainCollection): Int {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(expenses.indexOf(item))
        selectionCountListener?.invoke(selectedItems.size)
        return selectedItems.size
    }

    override fun getItemCount(): Int = expenses.size

    override fun getItemViewType(position: Int): Int = when (expenses[position]) {
        is ExpensesMainCollection.Header -> TYPE_HEADER
        is ExpensesMainCollection.Row -> TYPE_LIST
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_HEADER -> ExpensesHeaderViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_title_item, parent, false)
            )
            else -> ExpensesViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.expenses_item, parent, false),
                clickListener//clickEventDelegate
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ExpensesViewHolder -> {
                val item = expenses[position]
                holder.bind(item, selectedItems.contains(item))
            }
            is ExpensesHeaderViewHolder -> holder.bind(expenses[position])
        }
    }

    fun setList(newListExpenses: List<ExpensesMainCollection>) {
        selectedItems =
            selectedItems.filter { expense -> expenses.contains(expense) }.toMutableSet()
        selectionCountListener?.invoke(selectedItems.size)
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
    }

    fun getSelectedItems(): List<ExpensesMainCollection> = selectedItems.toList()
    fun setSelection(expenses: List<ExpensesMainCollection>) {
        if (expenses.isNotEmpty()) {
            selectedItems.addAll(expenses)
        }
    }

    fun endSelection() {
        val selectedItemsCache = selectedItems.toSet()
        selectedItems.clear()
        selectedItemsCache.forEach { selectedItem ->
            notifyItemChanged(expenses.indexOf(selectedItem))
        }
    }
}