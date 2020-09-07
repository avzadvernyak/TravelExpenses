package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "currency"
)

data class CurrencyTable (
    @PrimaryKey val name: String,
    val defCurrency: Int = 0
)