package m.kampukter.travelexpenses.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders"
)

data class Folders(
    @PrimaryKey val shortName: String,
    val description: String?
)
