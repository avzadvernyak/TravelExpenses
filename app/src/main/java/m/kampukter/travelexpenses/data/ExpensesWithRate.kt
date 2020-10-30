package m.kampukter.travelexpenses.data

import java.util.*

class ExpensesWithRate(
    val id: Long = 0L,
    val dateTime: Date,
    val expense: String,
    val sum: Float,
    val currency: String,
    val note: String,
    val rate: Float?,
    val exchangeDate: String?,
    val imageUri: String?
)