package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.Expenses

@Dao
interface ExpensesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expanses: Expenses): Long

    @Query("select * from expenses")
    fun getAll(): LiveData<List<Expenses>>

    @Query("select dateTime from expenses order by dateTime desc limit 1")
    fun getLastInputCurrent(): LiveData<Long>

    @Query("delete from expenses WHERE expenses.id = :selectedId")
    suspend fun deleteExpensesById(selectedId: Long)
}