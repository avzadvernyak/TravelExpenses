package m.kampukter.travelexpenses.data

import java.util.*

data class ExpensesExtendedView(
    val id: Long = 0L,
    val dateTime: Date = Date(),
    val expense_id: Long = 0,
    val expense: String = "",
    val sum: Double = 0.0,
    val currency: String = "",
    val note: String = "",
    val location: MyLocation? = null,
    val imageUri: String? = null,
    val rate: Float? = null,
    val folderId: Long = 0L,
    val folderName: String? = null
)
