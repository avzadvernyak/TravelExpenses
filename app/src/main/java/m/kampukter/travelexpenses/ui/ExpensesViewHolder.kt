package m.kampukter.travelexpenses.ui

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_item.view.*
import m.kampukter.travelexpenses.data.TravelExpensesView

class ExpensesViewHolder(
    itemView: View,
    private val clickEventDelegate: ClickEventDelegate<TravelExpensesView>
) : RecyclerView.ViewHolder(itemView) {
    fun bind(result: TravelExpensesView) {

        with(itemView) {

            setOnClickListener {
                clickEventDelegate.onClick(result)
            }
            setOnLongClickListener {
                clickEventDelegate.onLongClick(result)
                return@setOnLongClickListener true
            }
            sumTextView.text = result.sum.toString()
            expenseTextView.text = result.expenseName
            currencyTextView.text = result.currencyName
            noteTextView.text = result.note
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", result.dateTime)
        }
    }
}