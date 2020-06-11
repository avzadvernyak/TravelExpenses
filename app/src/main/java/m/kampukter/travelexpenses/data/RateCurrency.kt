package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "rateCurrency"
)

data class RateCurrency (
    @PrimaryKey val name: String,
    val exchangeDate: Date,
    val rate: Float
)