package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.Expense

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<Expense>)

    @Query("select * from expense")
    fun getAll(): LiveData<List<Expense>>

    @Query("select * from expense where name like :query limit 1")
    fun search(query: String): LiveData<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addExpense( expense: Expense)

    @Query("delete from expense WHERE expense.name = :selectedExpense")
    suspend fun deleteExpenseByName(selectedExpense: String)
}