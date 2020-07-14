package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings"
)
data class Settings(
    @PrimaryKey val userName: String,
    val defCurrency: Int = 0
)