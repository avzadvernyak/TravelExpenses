package m.kampukter.travelexpenses.data

import androidx.room.*
import java.util.*

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Expense::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("expense"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    ), ForeignKey(
        entity = CurrencyTable::class,
        parentColumns = arrayOf("name"),
        childColumns = arrayOf("currency_field")
    ), ForeignKey(
        entity = Folders::class,
        parentColumns = arrayOf("shortName"),
        childColumns = arrayOf("folder"),
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
    ],
    indices = [(Index(value = ["currency_field"], name = "idx_currency_field")),
        (Index(value = ["expense"], name = "idx_expense")),
        (Index(value = ["folder"], name = "idx_folder"))
    ]
)


data class Expenses(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "dateTime")
    val dateTime: Date,
    val expense: String,
    val sum: Double,
    @ColumnInfo(name = "currency_field")
    val currency: String,
    val note: String,
    @TypeConverters(MyTypeConverter::class)
    val location: MyLocation?,
    val imageUri: String?,
    val folder: String
)

