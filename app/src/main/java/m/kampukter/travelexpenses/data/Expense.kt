package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense",  indices = [(Index(value = ["id"], name = "idx_model_id"))])

data class Expense (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String
)