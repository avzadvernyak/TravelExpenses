package m.kampukter.travelexpenses.data.dao

import androidx.room.*
import m.kampukter.travelexpenses.data.RateCurrency

@Dao
interface RateCurrencyDao {
    @Query("delete from rateCurrency")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rateList: List<RateCurrency>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rateCurrency: RateCurrency): Long

    @Query("select count(*)  from rateCurrency where name = :name and Date(exchangeDate) = :date")
    suspend fun searchByDate(name: String, date: String): Long

}