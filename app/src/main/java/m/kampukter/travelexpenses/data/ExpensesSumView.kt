package m.kampukter.travelexpenses.data

import androidx.room.ColumnInfo

data class ExpensesSumView(
    @ColumnInfo(name = "expenses_sum")
    val sum: Float,
    @ColumnInfo(name = "expense_name")
    val expenseName: String

)