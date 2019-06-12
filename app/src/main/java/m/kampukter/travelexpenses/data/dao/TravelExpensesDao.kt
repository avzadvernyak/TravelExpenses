package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import m.kampukter.travelexpenses.data.TravelExpensesView

interface TravelExpensesDao : BasicDao<TravelExpensesView> {
    @Query(
        """select expenses.id AS id_records, currency.name AS currency_name,
            expense.name AS expense_name,
            expenses.expense_Id AS expenseId, expenses.currency_Id AS currencyId,
            expenses.sum, expenses.dateTime,expenses.note
        from expense
        inner join expenses on expenses.expense_Id=expense.id
        inner join currency on currency.id = expenses.currency_Id"""
    )
    fun getAll(): LiveData<List<TravelExpensesView>>
}