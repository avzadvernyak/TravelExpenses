package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import m.kampukter.travelexpenses.data.Settings

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings): Long

    @Query("select * from settings LIMIT 1")
    suspend fun getSettings(): Settings?

    @Query("select * from settings LIMIT 1")
    fun getSettingsLiveData(): LiveData<Settings?>

    @Query("select * from settings LIMIT 1")
    fun getAllSettings(): Flow<Settings>

    @Query("update settings set folder_id = :folderId where userName = :user")
    suspend fun updateFolderId(user: String, folderId: Long): Int

    @Query("select userName from settings")
    fun getUserName(): String
}