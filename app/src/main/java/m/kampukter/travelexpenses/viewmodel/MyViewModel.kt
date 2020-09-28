package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.mainApplication
import java.util.*

class MyViewModel(
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val rateRepository = mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()

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

    private val expensesDeleteTrigger = MutableLiveData<Boolean>()
    val expensesDeleteStatusMediatorLiveData = MediatorLiveData<Boolean>().apply {
        var expenses: Expenses? = null
        addSource(expensesDeleteTrigger) {
            if (it != null) {
                if (it) {
                    expenses?.let { _expenses ->
                        viewModelScope.launch {
                            expensesRepository.deleteExpensesById(_expenses.id)
                        }
                        postValue(true)
                    }
                }
            }
        }
        addSource(expensesById) { _expenses ->
            if (_expenses != null) expenses = _expenses
        }
    }

    fun expensesDeleteTrigger(trigger: Boolean) {
        expensesDeleteTrigger.postValue(trigger)
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

    // Update Expense
    private val expenseUpdateTrigger = MutableLiveData<String>()
    val expenseUpdateMediator = MediatorLiveData<Int>().apply {
        var oldExpenseName: String? = null
        addSource(expenseUpdateTrigger) {
            if (it != null) oldExpenseName?.let { oldName ->
                viewModelScope.launch {
                    postValue(expensesRepository.updateExpense(it, oldName))
                    queryExpense.postValue(it)
                }

            }
        }
        addSource(expenseById) {
            if (it != null) oldExpenseName = it.name
        }
    }

    fun updateExpense(expenseNewName: String) {
        expenseUpdateTrigger.postValue(expenseNewName)
    }


    fun deleteRate() {
        viewModelScope.launch { expensesRepository.deleteRate() }
    }

    val allRate: LiveData<List<RateCurrency>> = expensesRepository.getAllRate()

    fun saveSettings(settings: Settings) {
        viewModelScope.launch { expensesRepository.insertSettings(settings) }
    }

    val savedSettings = expensesRepository.getSettingsLiveData()

    fun startBackup(periodic: Periodic) {
        expensesRepository.startBackupWorker(periodic)
    }

    fun stopBackup() {
        expensesRepository.stopBackupWorker()
    }

    private val idProgram = MutableLiveData<String>()
    fun setIdProgram(id: String) {
        idProgram.postValue(id)
    }

    val restoreBackupLiveData: LiveData<BackupServer.Backup> =
        Transformations.switchMap(idProgram) { name ->
            expensesRepository.restoreBackupLiveData(name)
        }
    val expenseMediatorLiveData = MediatorLiveData<Pair<Expenses?, List<CurrencyTable>?>>().apply {
        var currentExpenses: Expenses? = null
        var currencyList: List<CurrencyTable> = emptyList()
        addSource(expensesById) {
            if (it != null) currentExpenses = it
            postValue(Pair(currentExpenses, currencyList))
        }
        addSource(currencyTableList) {
            if (it != null) currencyList = it
            postValue(Pair(currentExpenses, currencyList))
        }
    }

    val isSavingAllowed = MutableLiveData<Boolean?>(false)
    private val isSaveNewExpenses = MutableLiveData<Boolean>(false)

    fun saveNewExpenses() {
        isSaveNewExpenses.postValue(true)
    }

    private val bufferForSaveExpense = MutableLiveData<Expenses?>()
    val bufferExpensesMediatorLiveData =
        MediatorLiveData<Pair<Expenses?, List<CurrencyTable>?>>().apply {
            var expenses: Expenses? = null
            var currencyList: List<CurrencyTable> = emptyList()
            addSource(bufferForSaveExpense) {
                expenses = it
                postValue(Pair(expenses, currencyList))
                isSavingAllowed.postValue(
                    it != null && !it.currency.isBlank() && (it.sum != 0.0) && !it.note.isBlank() && !it.expense.isBlank()
                )
            }
            addSource(currencyTableList) {
                if (it != null) currencyList = it
                postValue(Pair(expenses, currencyList))
            }
            addSource(isSaveNewExpenses) {
                if (it) expenses?.let { _expenses ->
                    viewModelScope.launch {
                        expensesRepository.addExpenses(_expenses)
                        isSaveNewExpenses.postValue(false)
                    }
                }

            }
        }

    fun setBufferExpenses(expenses: Expenses?) {
        bufferForSaveExpense.postValue(expenses)
    }

    private val triggerForCurrencyExchange = MutableLiveData<Boolean>()
    private val dateForCurrencyExchange = MutableLiveData<Date>().apply {
        postValue(Calendar.getInstance().time)
    }

    fun setDateForCurrencyExchange(date: Date) {
        dateForCurrencyExchange.postValue(date)
    }

    fun setTriggerForCurrencyExchange() {
        triggerForCurrencyExchange.postValue(true)
    }

    val currentExchangeRate: LiveData<ResultCurrentExchangeRate> =
        MediatorLiveData<ResultCurrentExchangeRate>().apply {
            var findDate: Date? = null
            fun update() {
                viewModelScope.launch {
                    rateRepository?.let { repository ->
                        findDate?.let { date -> postValue(repository.getCurrentRate(date)) }
                    }
                }
            }
            addSource(dateForCurrencyExchange) {
                if (it != null) {
                    findDate = it
                    update()
                }
            }
            addSource(triggerForCurrencyExchange) {
                if (it != null) update()
            }
        }
}