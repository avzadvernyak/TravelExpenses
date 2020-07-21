package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "rateCurrency",
    primaryKeys = ["name", "exchangeDate"]
)

data class RateCurrency (
    val name: String,
    val exchangeDate: Date,
    val rate: Float
)