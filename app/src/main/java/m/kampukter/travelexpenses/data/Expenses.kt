package m.kampukter.travelexpenses.data

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    val sum: Double,
    @ColumnInfo(name = "currency_field")
    val currency: String,
    val note: String,
    @TypeConverters(MyTypeConverter::class)
    val location: MyLocation?,
    val imageUri: String?
)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Expenses ADD COLUMN location TEXT default null")
        database.execSQL(
            "ALTER TABLE Expenses ADD COLUMN imageUri TEXT default null")
    }
}