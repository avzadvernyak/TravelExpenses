package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import m.kampukter.travelexpenses.data.Currency

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<Currency>)

    @Query("select * from currency")
    fun getAll(): LiveData<List<Currency>>

    @Query("select * from currency where id like :query")
    fun search(query: String): LiveData<List<Currency>>

    @Query("select * from currency where defCurrency = 1")
    fun searchDefault(): LiveData<Currency>

    @Query("select * from currency where name = :query limit 1")
    fun searchByName(query: String): LiveData<Currency>

    @Query(" update currency set defCurrency = 1 where currency.id = :defaultId")
    suspend fun setDefault(defaultId: Long )

    @Query("update currency set defCurrency = 0 ")
    suspend fun resetDef()

    @Query("delete from currency")
    suspend fun deleteAll()

}