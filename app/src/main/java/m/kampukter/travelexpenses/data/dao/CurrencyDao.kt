package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import m.kampukter.travelexpenses.data.CurrencyTable

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CurrencyTable>)

    @Query("select * from currency")
    fun getAllLiveData(): LiveData<List<CurrencyTable>>

    @Query("select * from currency")
    fun getAllFlow(): Flow<List<CurrencyTable>>

    @Query("select * from currency")
    suspend fun getAll(): List<CurrencyTable>

    @Query("update currency set defCurrency = case when currency.name = :defaultCurrency then 1 else 0 end")
    suspend fun setDefault(defaultCurrency: String )

}