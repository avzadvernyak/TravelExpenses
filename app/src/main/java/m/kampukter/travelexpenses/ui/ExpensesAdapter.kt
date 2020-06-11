package m.kampukter.travelexpenses.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import m.kampukter.travelexpenses.R
import m.kampukter.travelexpenses.data.Expenses

class ExpensesAdapter(private val clickEventDelegate: ClickEventDelegate<Expenses>) :
    RecyclerView.Adapter<ExpensesViewHolder>() {
    private var expenses: List<Expenses>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        return ExpensesViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.expenses_item, parent, false),
            clickEventDelegate
        )
    }

    override fun getItemCount(): Int {
        return expenses?.size ?: 0
    }

    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        expenses?.get(position)?.let { item ->
            holder.bind(item)
        }
    }

    fun setList(list: List<Expenses>) {
        this.expenses = list
        notifyDataSetChanged()
    }
}