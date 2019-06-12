package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "currency"
)

data class Currency (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String
)