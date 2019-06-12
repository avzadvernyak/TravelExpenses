package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import m.kampukter.travelexpenses.data.Currency

interface CurrencyDao: BasicDao<Currency> {
    @Query("select * from currency")
    fun getAll(): LiveData<List<Currency>>

    @Query("select * from currency where name like :query")
    fun search(query: String): LiveData<List<Currency>>

    @Query("select * from currency where id = :currencyId limit 1")
    fun getModelId(currencyId: Long ): LiveData<Currency>

    @Query("delete from currency")
    suspend fun deleteAll()
}