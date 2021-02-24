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

    @Query("select * from expenses where expenses.folder = :folder")
    fun getAll(folder:String): LiveData<List<Expenses>>

    @Query("select * from expenses")
    suspend fun getAllExpenses(): List<Expenses>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder = :folder 
            group by expenses.dateTime
            order by expenses.dateTime desc  
           """
    )
    fun getAllExpensesWithRate(folder: String): LiveData<List<ExpensesWithRate>>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.note LIKE :searchString
            group by expenses.dateTime
            order by expenses.dateTime desc  
           
           """
    )
    fun getSearchExpensesWithRate( searchString: String): LiveData<List<ExpensesWithRate>>


    @Query("delete from expenses WHERE expenses.id = :selectedId")
    suspend fun deleteExpensesById(selectedId: Long)

    @Query("select * from expenses where id = :id")
    fun getExpensesById(id: Long): LiveData<Expenses>

    @Query("select count(expense) from expenses where expense = :name")
    suspend fun getExpensesCount(name: String): Long

    @Query("select count(folder) from expenses where folder = :name")
    suspend fun getFoldersCount(name: String): Long

    @Query(
        """ select sum(sum) AS sum, expense AS name, currency_field AS note 
            from expenses 
            where expenses.folder = :folder
            group by  expense,currency_field    
            """
    )
    fun getSumExpenses(folder: String): LiveData<List<ReportSumView>>

    @Query(
        """ select sum(sum) AS sum, currency_field AS name, null AS note 
            from expenses
            where expenses.folder = :folder
            group by  currency_field 
            """
    )
    fun getSumCurrency(folder: String): LiveData<List<ReportSumView>>

    @Query(
        """select currency_field, Date(dateTime) as dateRate 
        from expenses 
        group by date(dateTime), currency_field """
    )
    suspend fun getInfoForRate(): List<InfoForRate>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder = :folder 
            group by expenses.dateTime
            order by expenses.dateTime desc  
            
           """
    )
    fun getExpenses(folder: String): LiveData<List<ExpensesWithRate>>

    @Query("delete from expenses WHERE expenses.id IN (:selected)")
    suspend fun deleteIdList(selected: Set<Long>)
}