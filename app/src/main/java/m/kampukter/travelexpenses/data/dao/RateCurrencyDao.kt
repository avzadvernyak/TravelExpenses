package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.RateCurrency
import java.util.*

@Dao
interface RateCurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateCurrency: RateCurrency): Long

    @Query("select * from rateCurrency where name like :query limit 1")
    fun search(query: String): LiveData<RateCurrency>

    @Query("select * from rateCurrency ")
    suspend fun getAll(): List<RateCurrency>

    @Query("select * from rateCurrency where name like :name and date(exchangeDate) = date(:date)")
    suspend fun searchByDate(name: String, date: Long): List<RateCurrency>
}