package m.kampukter.travelexpenses.ui

import android.text.format.DateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_item.view.*
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.mainApplication
import java.text.DecimalFormat

class ExpensesViewHolder(
    itemView: View,
    private val clickEventDelegate: ClickEventDelegate<ExpensesWithRate>
) : RecyclerView.ViewHolder(itemView) {
    private val defaultProgramCurrency = mainApplication.getActiveCurrencySession()
    fun bind(result: ExpensesWithRate) {

        with(itemView) {

            setOnClickListener {
                clickEventDelegate.onClick(result)
            }
            setOnLongClickListener {
                clickEventDelegate.onLongClick(result)
                return@setOnLongClickListener true
            }
            sumTextView.text = result.sum.toString()
            expenseTextView.text = result.expense
            currencyTextView.text = result.currency
            noteTextView.text = result.note
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", result.dateTime)
            val pattern = when (defaultProgramCurrency ){
                // Гривна по умолчанию
                1 -> "######.## UAH"
                // Рубль по умолчению
                2 -> "######.## RUB"
                // Белорусский Рубль по умолчению
                3 -> "####.#### BYN"
                else -> "######.##"
            }
            result.rate?.let { rateTextView.text = DecimalFormat( pattern ).format(result.sum * it) }
        }
    }
}