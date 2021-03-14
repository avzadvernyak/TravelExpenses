package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense")

data class Expense (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String
)