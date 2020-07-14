package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.ExpensesWithRate
import m.kampukter.travelexpenses.data.InfoForRate
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
    suspend fun getAllExpenses(): List<Expenses>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate   
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null
            group by expenses.dateTime
            order by expenses.dateTime  
           """
    )
    fun getAllExpensesWithRate(): LiveData<List<ExpensesWithRate>>


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

    @Query(
        """select currency_field, Date(dateTime) as dateRate 
        from expenses 
        group by date(dateTime), currency_field """
    )
    suspend fun getInfoForRate(): List<InfoForRate>
}