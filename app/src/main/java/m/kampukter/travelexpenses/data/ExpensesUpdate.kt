package m.kampukter.travelexpenses.data

import java.util.*

data class ExpensesUpdate(
    val expense_id: Long,
    val sum: Double,
    val currency: String,
    val note: String,
    val dateTime: Date?,
    val location: MyLocation?,
    val imageUri: String?,
    val folderId: Long?
) {
    constructor() : this(
        dateTime = null,
        expense_id = 0,
        currency = "",
        sum = 0.0,
        note = "",
        location = null,
        imageUri = null,
        folderId = null
    )
}
