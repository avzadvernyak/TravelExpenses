package m.kampukter.travelexpenses.ui.expenses

import android.text.format.DateFormat
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.expenses_item.view.*
import kotlinx.android.synthetic.main.expenses_title_item.view.*
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_BYN
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_RUB
import m.kampukter.travelexpenses.DEFAULT_CURRENCY_CONST_UAH
import m.kampukter.travelexpenses.data.ExpensesMainCollection
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.ui.ClickEventDelegate
import java.text.DecimalFormat

class ExpensesViewHolder(
    itemView: View,
    private val clickEventDelegate: ClickEventDelegate<ExpensesMainCollection>
) : RecyclerView.ViewHolder(itemView) {
    private val defaultProgramCurrency = mainApplication.getActiveCurrencySession()
    fun bind(item: ExpensesMainCollection, isSelected: Boolean) {

        val data = (item as ExpensesMainCollection.Row).expenses

        with(itemView) {
            setSelected(isSelected)
            setOnClickListener {
                clickEventDelegate.onClick(item)
            }
            setOnLongClickListener {
                clickEventDelegate.onLongClick(item)
                return@setOnLongClickListener true
            }
            sumTextView.text = data.sum.toString()
            expenseTextView.text = data.expense
            currencyTextView.text = data.currency
            noteTextView.text = data.note
            dateTimeTextView.text = DateFormat.format("dd/MM/yyyy HH:mm", data.dateTime)
            val pattern = when (defaultProgramCurrency) {
                // Гривна по умолчанию
                DEFAULT_CURRENCY_CONST_UAH -> "######.## UAH"
                // Рубль по умолчению
                DEFAULT_CURRENCY_CONST_RUB -> "######.## RUB"
                // Белорусский Рубль по умолчению
                DEFAULT_CURRENCY_CONST_BYN -> "####.#### BYN"
                else -> "######.##"
            }
            data.rate?.let {
                rateTextView.text = DecimalFormat(pattern).format(data.sum * it)
            }
            attachmentImageView.visibility =
                if (data.imageUri == null) View.INVISIBLE else View.VISIBLE
        }

    }
}