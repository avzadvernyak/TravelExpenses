package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders"
)

data class Folders(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val shortName: String,
    val description: String?
)
