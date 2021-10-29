package m.kampukter.travelexpenses.data

import androidx.room.*
import java.util.*

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Expense::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("expense_id"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = CurrencyTable::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("currency_field")
    ), ForeignKey(
        entity = Folders::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("folder_id"),
        onDelete = ForeignKey.CASCADE
    )
    ],
    indices = [(Index(value = ["currency_field"], name = "idx_currency_field")),
        (Index(value = ["expense_id"], name = "idx_expense")),
        (Index(value = ["folder_id"], name = "idx_folder"))
    ]
)


data class Expenses(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "dateTime")
    val dateTime: Date,
    val expense_id: Long,
    val sum: Double,
    @ColumnInfo(name = "currency_field")
    val currency: String,
    val note: String,
    @TypeConverters(MyTypeConverter::class)
    val location: MyLocation?,
    val imageUri: String?,
    val folder_id: Long
)

