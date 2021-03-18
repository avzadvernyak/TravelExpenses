package m.kampukter.travelexpenses.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import m.kampukter.travelexpenses.data.Expense

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<Expense>)

    @Query("select * from expense")
    fun getAllFlow(): Flow<List<Expense>>

    @Query("select * from expense")
    suspend fun getAll(): List<Expense>

    @Query("select * from expense where name like :query limit 1")
    fun search(query: String): Flow<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addExpense( expense: Expense)

    @Query("delete from expense WHERE expense.id = :expenseId")
    suspend fun deleteExpense( expenseId: Long)

    @Query("update expense set name = :expenseName where id = :id ")
    suspend fun updateRecord( id: Long, expenseName: String): Int
}