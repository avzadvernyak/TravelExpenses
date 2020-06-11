package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense")

data class Expense (
    @PrimaryKey val name: String
)