package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import m.kampukter.travelexpenses.data.CurrencyTable

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CurrencyTable>)

    @Query("select * from currency")
    fun getAllLiveData(): LiveData<List<CurrencyTable>>

    @Query("select * from currency")
    suspend fun getAll(): List<CurrencyTable>

    @Query(" update currency set defCurrency = 1 where currency.name = :defaultCurrency")
    suspend fun setDefault(defaultCurrency: String )

    @Query("update currency set defCurrency = 0 ")
    suspend fun resetDef()

}