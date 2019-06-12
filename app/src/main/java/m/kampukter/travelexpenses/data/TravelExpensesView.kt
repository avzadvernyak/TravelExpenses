package m.kampukter.travelexpenses.data

import androidx.room.ColumnInfo

data class TravelExpensesView (
    @ColumnInfo(name = "id_records")
    val id: Long,
    val sum: Float,
    val dateTime: Long,
    val currencyId: Long,
    @ColumnInfo(name = "currency_name")
    val currencyName: String,
    val expenseId: Long,
    @ColumnInfo(name = "expense_name")
    val expenseName: String,
    val note: String
)