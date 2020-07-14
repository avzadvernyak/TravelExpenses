package m.kampukter.travelexpenses.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.RateCurrency
import m.kampukter.travelexpenses.data.Settings

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings): Long

    @Query("select * from settings LIMIT 1")
    suspend fun getSettings(): Settings?

}