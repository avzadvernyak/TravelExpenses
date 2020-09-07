package m.kampukter.travelexpenses.data

import androidx.room.*
import java.util.*

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Expense::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("expense"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = CurrencyTable::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("currency_field")
    )
    ],
    indices = [(Index(value = ["currency_field"], name = "idx_currency_field")),
        (Index(value = ["expense"], name = "idx_expense"))]
)

data class Expenses(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "dateTime")
    val dateTime: Date,
    val expense: String,
    val sum: Float,
    @ColumnInfo(name = "currency_field")
    val currency: String,
    val note: String
)