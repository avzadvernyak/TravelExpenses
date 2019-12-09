package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import m.kampukter.travelexpenses.data.ExpensesSumView
import m.kampukter.travelexpenses.data.TravelExpensesView

@Dao
interface TravelExpensesDao {
    /*
    select expense.id, (select sum(expenses.sum) from expenses where expenses.id_expense = expense.id)  from expense
     */
    @Query(
        """select expense.name AS expense_name, (select sum(expenses.sum) from expenses where expenses.expense_Id = expense.id) AS expenses_sum  
            from expense"""
    )
    fun getSumExpenses(): LiveData<List<ExpensesSumView>>
    @Query(
        """select expenses.id AS id_records, currency.name AS currency_name,
            expense.name AS expense_name,
            expenses.expense_Id AS expenseId, expenses.currency_Id AS currencyId,
            expenses.sum, expenses.dateTime,expenses.note
        from expense
        inner join expenses on expenses.expense_Id=expense.id
        inner join currency on currency.id = expenses.currency_Id
        order by expenses.dateTime desc"""
    )
    fun getAll(): LiveData<List<TravelExpensesView>>

    @Query(
        """select expenses.id AS id_records, currency.name AS currency_name,
            expense.name AS expense_name,
            expenses.expense_Id AS expenseId, expenses.currency_Id AS currencyId,
            expenses.sum, expenses.dateTime,expenses.note
        from expense
        inner join expenses on expenses.expense_Id=expense.id
        inner join currency on currency.id = expenses.currency_Id"""
    )
    suspend fun getAllExpenses(): List<TravelExpensesView>

    @Query(
        """select expenses.id AS id_records, currency.name AS currency_name,
            expense.name AS expense_name,
            expenses.expense_Id AS expenseId, expenses.currency_Id AS currencyId,
            expenses.sum, expenses.dateTime,expenses.note
        from expense
        inner join expenses on expenses.expense_Id=expense.id
        inner join currency on currency.id = expenses.currency_Id
        where expenses.id = :idRecords
        order by expenses.dateTime desc
        """
    )
    fun getRecordById(idRecords: Long): LiveData<TravelExpensesView>


}