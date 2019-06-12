package m.kampukter.travelexpenses.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Expense::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("expense_Id")
    ), ForeignKey(
        entity = Currency::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("currency_Id")
    )]
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