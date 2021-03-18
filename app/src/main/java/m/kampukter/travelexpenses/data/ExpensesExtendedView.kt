package m.kampukter.travelexpenses.data

import java.util.*

data class ExpensesExtendedView(
    val id: Long = 0L,
    val dateTime: Date,
    val expense_id: Long,
    val expense: String,
    val sum: Double,
    val currency: String,
    val note: String,
    val location: MyLocation?,
    val imageUri: String?,
    val rate: Float?,
    val folderId: Long,
    val folderName: String?
)
