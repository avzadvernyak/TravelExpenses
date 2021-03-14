package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import m.kampukter.travelexpenses.data.*

@Dao
interface ExpensesDao {
    @Query("delete from expenses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expanses: Expenses): Long

    @Query("select * from expenses where expenses.folder_id = :folderId")
    fun getAll( folderId: Long ): Flow<List<Expenses>>

    @Query("select * from expenses")
    suspend fun getAllExpenses(): List<Expenses>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
           """
    )
    fun getAllExpensesWithRate( folderId: Long ): Flow<List<ExpensesWithRate>>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expense.name as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            LEFT JOIN expense ON expenses.expense_id = expense.name 
            where ((date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.note LIKE :searchString) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
           
           """
    )
    fun getSearchExpenses(searchString: String, folderId: Long): Flow<List<ExpensesExtendedView>>


    @Query("delete from expenses WHERE expenses.id = :selectedId")
    suspend fun deleteExpensesById(selectedId: Long)

    @Query("select * from expenses where id = :id")
    fun getExpensesById(id: Long): LiveData<Expenses>

    @Query("select count(expense_id) from expenses where expense_id = :name")
    suspend fun getExpensesCount(name: String): Long

    @Query("select count(folder_id) from expenses where folder_id = :folderId")
    suspend fun getFoldersCount(folderId: Long): Long

    @Query(
        """ select sum(sum) AS sum, expense_id AS name, currency_field AS note 
            from expenses 
            where expenses.folder_id = :folder_id
            group by  expense_id,currency_field    
            """
    )
    fun getSumExpenses(folder_id: Long): Flow<List<ReportSumView>>

    @Query(
        """ select sum(sum) AS sum, currency_field AS name, null AS note 
            from expenses
            where expenses.folder_id = :folder_id
            group by  currency_field 
            """
    )
    fun getSumCurrency(folder_id: Long): Flow<List<ReportSumView>>

    @Query(
        """select currency_field, Date(dateTime) as dateRate 
        from expenses 
        group by date(dateTime), currency_field """
    )
    suspend fun getInfoForRate(): List<InfoForRate>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
            
           """
    )
    fun getExpenses(folderId: Long): LiveData<List<ExpensesWithRate>>

    @Query("delete from expenses WHERE expenses.id IN (:selected)")
    suspend fun deleteIdList(selected: Set<Long>)

    @Query("update expenses set folder_id = :newFolderId WHERE expenses.id IN (:selected)")
    suspend fun moveIdList(selected: Set<Long>, newFolderId: Long)

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
            
           """
    )
    fun getExpensesFlow(folderId: Long): Flow<List<ExpensesWithRate>>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expense.name as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri  
            from expenses
            LEFT JOIN expense ON expenses.expense_id = expense.name 
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
            
           """
    )
    fun getExpensesView(folderId: Long): Flow<List<ExpensesExtendedView>>
}