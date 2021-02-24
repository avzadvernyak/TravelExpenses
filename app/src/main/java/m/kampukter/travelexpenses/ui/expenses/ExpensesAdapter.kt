package m.kampukter.travelexpenses.ui.expenses

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.ui.ClickEventDelegate

private const val TYPE_HEADER: Int = 2
private const val TYPE_LIST: Int = 1

class ExpensesAdapter(
    private val context: Context,
    private var clickListener: ((ExpensesMainCollection) -> Unit)? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expenses = emptyList<ExpensesMainCollection>()

    private var selectedItems = mutableSetOf<ExpensesMainCollection>()

    private var isInSelection = false
    private var actionModeCallback: ActionMode.Callback? = null
    private var selectionCountListener: ((Int) -> Unit)? = null


    private val clickEventDelegate: ClickEventDelegate<ExpensesMainCollection> =
        object : ClickEventDelegate<ExpensesMainCollection> {
            override fun onClick(item: ExpensesMainCollection) {
                if (isInSelection) {
                    toggleItemSelection(item)
                } else {
                    clickListener?.invoke(item)
                }
            }

            override fun onLongClick(item: ExpensesMainCollection) {
                if (!isInSelection) {
                    actionModeCallback?.let { callback ->
                        (context as AppCompatActivity).startSupportActionMode(callback)
                        isInSelection = true
                        toggleItemSelection(item)
                    }
                }
            }
        }

    private fun toggleItemSelection(item: ExpensesMainCollection) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        notifyItemChanged(expenses.indexOf(item))
        selectionCountListener?.invoke(selectedItems.size)
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
                clickEventDelegate
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

    fun enableActionMode(
        actionModeCallback: ActionMode.Callback,
        selectionCountListener: (Int) -> Unit
    ) {
        this.actionModeCallback = actionModeCallback
        this.selectionCountListener = selectionCountListener
    }

    fun getSelectedItems(): List<ExpensesMainCollection> = selectedItems.toList()
    fun setSelection(expenses: List<ExpensesMainCollection>) {
        if (expenses.isNotEmpty()) {
            actionModeCallback?.let<ActionMode.Callback, Unit> { callback ->
                (context as AppCompatActivity).startSupportActionMode(callback)
                isInSelection = true
                selectedItems.addAll(expenses)
                selectionCountListener?.invoke(selectedItems.size)
            }
        }
    }

    fun endSelection() {
        isInSelection = false
        val selectedItemsCache = selectedItems.toSet()
        selectedItems.clear()
        selectedItemsCache.forEach { selectedItem ->
            notifyItemChanged(expenses.indexOf(selectedItem))
        }
    }
}