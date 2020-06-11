package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.RateCurrency

@Dao
interface RateCurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateCurrency: RateCurrency): Long

    @Query("select * from rateCurrency where name like :query limit 1")
    fun search(query: String): LiveData<RateCurrency>
}