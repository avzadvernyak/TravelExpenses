package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
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

    @Query("select * from rateCurrency where name like :query limit 1")
    fun search(query: String): LiveData<RateCurrency>

    @Query("select * from rateCurrency order by exchangeDate")
    fun getAll(): LiveData<List<RateCurrency>>

    //@Query("select * from rateCurrency where name = :name and date(exchangeDate) = date(:date)")
    //
    @Query("select count(*)  from rateCurrency where name = :name and Date(exchangeDate) = :date")
    suspend fun searchByDate(name: String, date: String): Long

}