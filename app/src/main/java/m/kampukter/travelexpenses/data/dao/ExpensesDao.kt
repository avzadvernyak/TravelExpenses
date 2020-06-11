package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.ReportSumView

@Dao
interface ExpensesDao {
    @Query("delete from expenses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expanses: Expenses): Long

    @Query("select * from expenses")
    fun getAll(): LiveData<List<Expenses>>

    @Query("select * from expenses")
    fun getAllExpenses(): List<Expenses>

    @Query("delete from expenses WHERE expenses.id = :selectedId")
    suspend fun deleteExpensesById(selectedId: Long)

    @Update
    suspend fun updateRecord(expanses: Expenses)

    @Query("select * from expenses where id = :id")
    fun getExpensesById(id: Long): LiveData<Expenses>

    @Query("select count(expense) from expenses where expense = :name")
    fun getExpensesCount(name: String): Long

    @Query(
        """ select sum(sum) AS sum, expense AS name, currency_field AS note 
            from expenses 
            group by  expense,currency_field    
            """
    )
    fun getSumExpenses(): LiveData<List<ReportSumView>>

    @Query(
        """ select sum(sum) AS sum, currency_field AS name, null AS note 
            from expenses 
            group by  currency_field    
            """
    )
    fun getSumCurrency(): LiveData<List<ReportSumView>>
}