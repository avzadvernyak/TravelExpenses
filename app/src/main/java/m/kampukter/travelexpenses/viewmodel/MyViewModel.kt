package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.repository.CurrencyRepository
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.TravelExpensesRepository

class MyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val expenseRepository: ExpenseRepository,
    private val expensesRepository: ExpensesRepository,
    private val travelExpensesRepository: TravelExpensesRepository
) : ViewModel() {
    /*
    *Получение коллекции Expenses по Expense_Id
    */
    private val expenseIdFind = MutableLiveData<Long>()

    fun setQueryExpensesId(query: Long) {
        expenseIdFind.postValue(query)
    }

    val expensesByExpenseId: LiveData<List<Expenses>> =
        Transformations.switchMap(expenseIdFind) { query -> expensesRepository.getListByExpenseId(query) }

    /*
    *Получение типа валюты последней введенной суммы
    */
    val lastInputCurrency = expensesRepository.getLastInputCurrent()

    fun addExpenses(expenses: Expenses) {
        expensesRepository.addExpenses(expenses)
    }

    fun updateExpenses(expenses: Expenses) {
        expensesRepository.updateExpenses(expenses)
    }

    val expenses: LiveData<List<TravelExpensesView>> = travelExpensesRepository.getAll()

    private val expensesFindId = MutableLiveData<Long>()
    fun setQueryTravelExpensesId(query: Long) {
        expensesFindId.postValue(query)
    }

    val expensesById: LiveData<TravelExpensesView> =
        Transformations.switchMap(expensesFindId) { query -> travelExpensesRepository.getRecordById(query) }

    fun expensesDelete(expensesId: Long) {
        expensesRepository.deleteExpensesById(expensesId)
    }


    fun setDefCurrency(query: Long) {
        currencyRepository.setDefCurrency(query)
    }

    fun resetDef() {
        currencyRepository.resetDef()
    }

    val defCurrency: LiveData<Currency> = currencyRepository.getDefCurrency()
    val currencyList: LiveData<List<Currency>> = currencyRepository.getCurrencyAll()


    private val queryExpenseId = MutableLiveData<String>()
    fun setQueryExpenseId(query: String) {
        queryExpenseId.postValue(query)
    }

    val expenseById: LiveData<Expense> =
        Transformations.switchMap(queryExpenseId) { query -> expenseRepository.getExpenseById(query) }

    val expenseList: LiveData<List<Expense>> = expenseRepository.getExpenseAll()
    fun addExpense(expense: Expense) {
        expenseRepository.addExpense(expense)
    }

    fun delExpense(expenseId: Long) {
        expenseRepository.deleteExpenseById(expenseId)
    }

    /*
    * Для удаления Expense. Код Олега
    */

    private val expenseDeletionTrigger = MutableLiveData<ExpenseDeletionRequest>()
    val expenseDeletionResultLiveData: LiveData<ExpenseDeletionResult> =
        Transformations.switchMap(expenseDeletionTrigger) { request ->
            expenseRepository.deleteExpense(request.id, request.isForced)
        }

    fun deleteExpense(expenseId: Long, isForced: Boolean) {
        expenseDeletionTrigger.postValue(ExpenseDeletionRequest(expenseId, isForced))
    }

    data class ExpenseDeletionRequest(
        val id: Long,
        val isForced: Boolean
    )

}