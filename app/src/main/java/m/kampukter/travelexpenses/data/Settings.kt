package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
    foreignKeys = [ForeignKey(
        entity = Folders::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("folder_id"),
    )]
)
data class Settings(
    @PrimaryKey val userName: String,
    val defCurrency: Int = 0,
    val backupPeriod: Int = 0,
    val statusGPS: Int = 0,
    val folder_id: Long = 0
)