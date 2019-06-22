package m.kampukter.travelexpenses.data

import androidx.room.*

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Expense::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("expense_Id"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Currency::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("currency_Id")
    )],
    indices = [(Index(value = ["expense_Id"], name = "idx_expense_id")),
        (Index(value = ["currency_Id"], name = "idx_currency_id"))]
)

data class Expenses(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dateTime: Long,
    @ColumnInfo(name = "expense_Id")
    val expense: Long,
    val sum: Float,
    @ColumnInfo(name = "currency_Id")
    val currency: Long,
    val note: String
)