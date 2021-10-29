package m.kampukter.travelexpenses.data.dao

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

    //@Query("select * from expenses")
    @Query(
    """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expenses.note as note, expenses.sum as sum, 
                            expenses.imageUri as imageUri, expenses.location as location , expenses.folder_id as folderId,
                            expense.name as expense , folders.shortName as folderName  
            from expenses
            LEFT JOIN expense ON expenses.expense_id = expense.id
            LEFT JOIN folders ON expenses.folder_id = folders.id 
            """

    )
    suspend fun getAllExpenses(): List<ExpensesExtendedView>

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                         expenses.expense_id as expense_id, expense.name as expense, expenses.note as note, expenses.sum as sum, 
                         rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri,
                         expenses.folder_id as folderId, folders.shortName as folderName
            from expenses
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            LEFT JOIN expense ON expenses.expense_id = expense.id
            LEFT JOIN folders ON expenses.folder_id = folders.id 
            where ((date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.note LIKE :searchString) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
           
           """
    )
    fun getSearchExpenses(searchString: String, folderId: Long): Flow<List<ExpensesExtendedView>>

    @Query(
        """ select sum(sum) AS sum, expense.name AS name, currency_field AS note 
            from expenses 
            LEFT JOIN expense ON expenses.expense_id = expense.id 
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

    @Query("delete from expenses WHERE expenses.id IN (:selected)")
    suspend fun deleteIdList(selected: Set<Long>)

    @Query("update expenses set folder_id = :newFolderId WHERE expenses.id IN (:selected)")
    suspend fun moveIdList(selected: Set<Long>, newFolderId: Long)

    @Query(
        """ select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expense.name as expense, expenses.note as note, expenses.sum as sum, 
                              rateCurrency.rate as rate, date(rateCurrency.exchangeDate) as exchangeDate , expenses.imageUri as imageUri,
                              expenses.location as location, expenses.folder_id as folderId
            from expenses
            LEFT JOIN expense ON expenses.expense_id = expense.id 
            LEFT JOIN rateCurrency ON expenses.currency_field = rateCurrency.name 
            where (date(expenses.dateTime) >= date(rateCurrency.exchangeDate) or rateCurrency.exchangeDate is null) and expenses.folder_id = :folderId 
            group by expenses.dateTime
            order by expenses.dateTime desc  
            
           """
    )
    fun getExpensesView(folderId: Long): Flow<List<ExpensesExtendedView>>

    @Query(""" select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expense.name as expense, expenses.note as note, expenses.sum as sum, 
                            expenses.imageUri as imageUri, expenses.location as location , expenses.folder_id as folderId   
            from expenses
            LEFT JOIN expense ON expenses.expense_id = expense.id 
            where expenses.id = :id"""
        )
    fun getExpensesById(id: Long): Flow<ExpensesExtendedView>

    @Query("update expenses set expense_id = :expenseId where id = :id")
    suspend fun updateExpense(id: Long, expenseId: Long)

    @Query("update expenses set currency_field = :name where id = :id")
    suspend fun updateCurrency(id: Long, name: String)

    @Query("update expenses set note = :note where id = :id")
    suspend fun updateNote(id: Long, note: String)

    @Query("update expenses set sum = :sum where id = :id")
    suspend fun updateSum(id: Long, sum: Double)

    @Query("update expenses set imageUri = :imageUri where id = :id")
    suspend fun updateImageUri(id: Long, imageUri: String?)

    @Query(""" select expenses.id as id, expenses.dateTime as dateTime, expenses.currency_field as currency,
                            expenses.expense_id as expense_id, expenses.note as note, expenses.sum as sum, 
                            expenses.imageUri as imageUri, expenses.location as location , expenses.folder_id as folderId,
                            expense.name as expense   
            from expenses
             LEFT JOIN expense ON expenses.expense_id = expense.id 
            where expenses.expense_id = :id"""
    )
    fun getExpensesByExpense(id: Long): Flow<List<ExpensesExtendedView>>
}