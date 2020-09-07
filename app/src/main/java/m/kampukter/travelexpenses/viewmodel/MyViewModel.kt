package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.mainApplication

class MyViewModel(
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val rateRepository = mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()

    private val reloadCurrentRate = MutableLiveData<Boolean>()
    fun startReloadCurrentRate() {
        reloadCurrentRate.postValue(true)
    }

    val currentExchangeRate: LiveData<ResultCurrentExchangeRate> = reloadCurrentRate.switchMap {
        liveData(context = viewModelScope.coroutineContext) {
            rateRepository?.let { emitSource(it.getCurrentRate()) }
        }
    }

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

    val expensesCSVForExport = Transformations.switchMap(expensesCSV) {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emitSource(expensesRepository.getAllForSend())
        }
    }

    /*
    * Удаление всех записей из Expenses
    */

    fun deleteAllExpenses() {
        viewModelScope.launch { expensesRepository.deleteAll() }
    }

    fun addExpenses(expenses: Expenses) {
        viewModelScope.launch { expensesRepository.addExpenses(expenses) }
    }

    fun updateExpenses(expenses: Expenses) {
        viewModelScope.launch { expensesRepository.updateExpenses(expenses) }
    }

    fun expensesDelete(expensesId: Long) {
        viewModelScope.launch { expensesRepository.deleteExpensesById(expensesId) }
    }

    val expenses: LiveData<List<Expenses>> = expensesRepository.getAll()
    val expensesWithRate = expensesRepository.getAllExpensesWithRate()

    private val expensesFindId = MutableLiveData<Long>()
    fun setQueryExpensesId(query: Long) {
        expensesFindId.postValue(query)
    }

    val expensesById: LiveData<Expenses> =
        Transformations.switchMap(expensesFindId) { query -> expensesRepository.getRecordById(query) }


    fun setDefCurrency(query: String) {
        expensesRepository.setDefCurrency(query)
    }

    fun resetDef() {
        expensesRepository.resetDef()
    }

    val currencyTableList: LiveData<List<CurrencyTable>> =
        expensesRepository.getCurrencyAllLiveData()


    private val queryExpense = MutableLiveData<String>()
    fun setQueryExpense(query: String) {
        queryExpense.postValue(query)
    }

    val expenseById: LiveData<Expense> =
        Transformations.switchMap(queryExpense) { query -> expensesRepository.getExpenseByName(query) }

    val expenseList: LiveData<List<Expense>> = expensesRepository.getExpenseAllLiveData()
    fun addExpense(expense: Expense) {
        expensesRepository.addExpense(expense)
    }

    /*
    * Для удаления Expense. Код Олега
    */

    private val expenseDeletionTrigger = MutableLiveData<ExpenseDeletionRequest>()
    val expenseDeletionResultLiveData: LiveData<ExpenseDeletionResult> =
        Transformations.switchMap(expenseDeletionTrigger) { request ->
            expensesRepository.deleteExpense(request.name, request.isForced)
        }

    fun deleteExpense(expenseName: String, isForced: Boolean) {
        expenseDeletionTrigger.postValue(ExpenseDeletionRequest(expenseName, isForced))
    }

    data class ExpenseDeletionRequest(
        val name: String,
        val isForced: Boolean
    )
    fun deleteRate() {
        viewModelScope.launch { expensesRepository.deleteRate() }
    }

    val allRate: LiveData<List<RateCurrency>> = expensesRepository.getAllRate()

    fun saveSettings(settings: Settings) {
        viewModelScope.launch { expensesRepository.insertSettings(settings) }
    }

    val savedSettings = expensesRepository.getSettingsLiveData()

    fun startBackup(periodic: Periodic) {expensesRepository.startBackupWorker(periodic)}
    fun stopBackup() {expensesRepository.stopBackupWorker()}

    private val idProgram = MutableLiveData<String>()
    fun setIdProgram(id: String) {
        idProgram.postValue(id)
    }
    val restoreBackupLiveData: LiveData<BackupServer.Backup> =
        Transformations.switchMap(idProgram) { name ->
            expensesRepository.restoreBackupLiveData(name)
        }

}