package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.repository.CurrencyRepository
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyRepository

class MyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val expenseRepository: ExpenseRepository,
    private val expensesRepository: ExpensesRepository,
    private val rateCurrencyRepository: RateCurrencyRepository
) : ViewModel() {
    /*
    * получение итогов по статьям и по валютам
    */
    fun getExpensesSum() = expensesRepository.getExpensesSum()
    fun getCurrencySum() = expensesRepository.getCurrencySum()

    /*
    * получение строки в CSV из коллекции Expenses для экспорта
    */
    private val expensesCSV = MutableLiveData<Boolean>()
    fun getExpensesCSV(query: Boolean) {
        expensesCSV.postValue(query)
    }
    val expensesCSVForExport: LiveData<String> =
        Transformations.switchMap(expensesCSV) { expensesRepository.getAllForSend() }
    /*
    * Удаление всех записей из Expenses
    */
    fun deleteAllExpenses() = expensesRepository.deleteAll()


    fun addExpenses(expenses: Expenses) {
        expensesRepository.addExpenses(expenses)
    }

    fun updateExpenses(expenses: Expenses) {
        expensesRepository.updateExpenses(expenses)
    }

    val expenses: LiveData<List<Expenses>> = expensesRepository.getAll()

    private val expensesFindId = MutableLiveData<Long>()
    fun setQueryExpensesId(query: Long) {
        expensesFindId.postValue(query)
    }

    val expensesById: LiveData<Expenses> =
        Transformations.switchMap(expensesFindId) { query -> expensesRepository.getRecordById(query) }

    fun expensesDelete(expensesId: Long) {
        expensesRepository.deleteExpensesById(expensesId)
    }


    fun setDefCurrency(query: String) {
        currencyRepository.setDefCurrency(query)
    }

    fun resetDef() {
        currencyRepository.resetDef()
    }

    val currencyList: LiveData<List<Currency>> = currencyRepository.getCurrencyAll()


    private val queryExpense = MutableLiveData<String>()
    fun setQueryExpense(query: String) {
        queryExpense.postValue(query)
    }

    val expenseById: LiveData<Expense> =
        Transformations.switchMap(queryExpense) { query -> expenseRepository.getExpenseByName(query) }

    val expenseList: LiveData<List<Expense>> = expenseRepository.getExpenseAll()
    fun addExpense(expense: Expense) {
        expenseRepository.addExpense(expense)
    }

    /*
    * Для удаления Expense. Код Олега
    */

    private val expenseDeletionTrigger = MutableLiveData<ExpenseDeletionRequest>()
    val expenseDeletionResultLiveData: LiveData<ExpenseDeletionResult> =
        Transformations.switchMap(expenseDeletionTrigger) { request ->
            expenseRepository.deleteExpense(request.name, request.isForced)
        }

    fun deleteExpense(expenseName: String, isForced: Boolean) {
        expenseDeletionTrigger.postValue(ExpenseDeletionRequest(expenseName, isForced))
    }

    data class ExpenseDeletionRequest(
        val name: String,
        val isForced: Boolean
    )

    fun getRateCurrency() {rateCurrencyRepository.getRateCurrencyNBU()}

}