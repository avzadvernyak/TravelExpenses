package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import m.kampukter.travelexpenses.data.Expenses

@Dao
interface ExpensesDao {
    @Query("delete from expenses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expanses: Expenses): Long

    @Query("select * from expenses")
    fun getAll(): LiveData<List<Expenses>>

    @Query("select dateTime from expenses order by dateTime desc limit 1")
    fun getLastInputCurrent(): LiveData<Long>

    @Query("delete from expenses WHERE expenses.id = :selectedId")
    suspend fun deleteExpensesById(selectedId: Long)

    @Update
    suspend fun updateRecord(expanses: Expenses)

    @Query("select * from expenses where expense_Id = :expenseId")
    fun getListByExpenseId(expenseId: Long): LiveData<List<Expenses>>

    @Query("select count(expense_Id) from expenses where expense_Id = :expenseId")
    fun getExpensesCount(expenseId: Long): Long

}