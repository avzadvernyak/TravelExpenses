package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
    foreignKeys = [ForeignKey(
        entity = Folders::class,
        parentColumns = arrayOf("shortName"),
        childColumns = arrayOf("folder"),
        onUpdate = ForeignKey.CASCADE
    )]
)
data class Settings(
    @PrimaryKey val userName: String,
    val defCurrency: Int = 0,
    val backupPeriod: Int = 0,
    val statusGPS: Int = 0,
    val folder: String = ""
)