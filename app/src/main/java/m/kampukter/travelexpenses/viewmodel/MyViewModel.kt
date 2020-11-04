package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.FSRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.mainApplication
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.*

class MyViewModel(
    private val expensesRepository: ExpensesRepository,
    private val fileSystemRepository: FSRepository
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

    private val currencyTableList: LiveData<List<CurrencyTable>> =
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

    // Удаление записи в Expense и связанных записей. В два этапа
    // 1 если нет связанных записей, удаляем сразу
    // 2 если есть связанные то удаление после подтверждения
    private val expenseDelTrigger = MutableLiveData<Boolean>()
    private val expenseDeleteName = MutableLiveData<String>()
    val expenseDeletionResult: LiveData<ExpenseDeletionResult> =
        MediatorLiveData<ExpenseDeletionResult>().apply {
            var lastExpenseName: String? = null
            var lastExpenseTrigger: Boolean? = null
            fun reset() {
                expenseDelTrigger.postValue(null)
                expenseDeleteName.postValue(null)
                postValue(null)
            }

            fun update() {
                val expenseName = lastExpenseName
                val expenseTrigger = lastExpenseTrigger

                if (expenseName == null || expenseTrigger == null) return

                viewModelScope.launch {
                    if (expenseTrigger) {
                        expensesRepository.deleteExpenseRecord(expenseName, true)
                        postValue(ExpenseDeletionResult.Success)
                        delay(1000)
                        reset()
                    } else {
                        val numberRec = expensesRepository.deleteExpenseRecord(
                            expenseName,
                            expenseTrigger
                        )
                        if (numberRec == 0L) {
                            expensesRepository.deleteExpenseRecord(expenseName, true)
                            postValue(ExpenseDeletionResult.Success)
                            delay(1000)
                            reset()
                        } else postValue(ExpenseDeletionResult.Warning(expenseName, numberRec))
                    }
                }
            }
            addSource(expenseDeleteName) {
                lastExpenseName = it
                update()
            }
            addSource(expenseDelTrigger) {
                lastExpenseTrigger = it
                update()
            }
        }

    fun deleteExpenseName(expenseName: String) {
        expenseDeleteName.postValue(expenseName)
    }

    fun deleteExpenseTrigger(isDelete: Boolean) {
        expenseDelTrigger.postValue(isDelete)
    }

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


    fun saveSettings(settings: Settings) {
        viewModelScope.launch { expensesRepository.insertSettings(settings) }
    }

    val savedSettings = expensesRepository.getSettingsLiveData()

    private val settingStatusGPS = MutableLiveData<Int>()
    fun setSettingStatusGPS(status: Int) {
        settingStatusGPS.postValue(status)
    }

    val savedSettingsLiveData = MediatorLiveData<Settings>().apply {
        var lastSettings: Settings? = null
        addSource(savedSettings) {
            if (it != null) lastSettings = it
            postValue(it)
        }
        addSource(settingStatusGPS) { statusGps ->
            lastSettings?.let {
                viewModelScope.launch {
                    expensesRepository.insertSettings(
                        it.copy(statusGPS = statusGps)
                    )
                }
            }
        }
    }

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

    val isSavingAllowed = MutableLiveData<Boolean?>()
    private val isSaveNewExpenses = MutableLiveData<Boolean>()

    fun saveNewExpenses() {
        isSaveNewExpenses.postValue(true)
    }

    private val bufferForSaveExpenseLocation = MutableLiveData<MyLocation?>()
    fun setBufferExpensesLocation(location: MyLocation) {
        bufferForSaveExpenseLocation.postValue(location)
    }

    private val bufferForSaveExpensePhoto = MutableLiveData<String?>()
    fun setBufferExpensesPhoto(file: String?) {
        bufferForSaveExpensePhoto.postValue(file)
    }


    private val bufferForSaveExpense = MutableLiveData<Expenses?>()
    val bufferExpensesMediatorLiveData =
        MediatorLiveData<Pair<Expenses?, List<CurrencyTable>?>>().apply {
            var lastExpenses: Expenses? = null
            var currencyList: List<CurrencyTable> = emptyList()
            addSource(bufferForSaveExpense) {
                lastExpenses = it
                postValue(Pair(lastExpenses, currencyList))
                isSavingAllowed.postValue(
                    it != null && !it.currency.isBlank() && (it.sum != 0.0) && !it.note.isBlank() && !it.expense.isBlank()
                )
            }
            addSource(currencyTableList) {
                if (it != null) currencyList = it
                postValue(Pair(lastExpenses, currencyList))
            }
            addSource(isSaveNewExpenses) {
                if (it) lastExpenses?.let { _expenses ->
                    viewModelScope.launch {
                        expensesRepository.addExpenses(_expenses)

                        //установка сохраняемой валюты как по умолчанию
                        expensesRepository.resetDef()
                        expensesRepository.setDefCurrency(_expenses.currency)

                        isSaveNewExpenses.postValue(false)
                    }
                }

            }
            addSource(bufferForSaveExpenseLocation) {
                if (it != null) {
                    lastExpenses = lastExpenses?.copy(location = it)
                    postValue(Pair(lastExpenses, currencyList))
                }
            }
            addSource(bufferForSaveExpensePhoto) {
                lastExpenses = lastExpenses?.copy(imageUri = it)
                postValue(Pair(lastExpenses, currencyList))
            }
        }

    fun setBufferExpenses(expenses: Expenses?) {
        bufferForSaveExpense.postValue(expenses)
    }


    private val triggerForCurrencyExchange = MutableLiveData<Boolean>()

    fun setDateForCurrencyExchange(date: Date?) {
        triggerForCurrencyExchange.postValue(true)
        if (date != null) expensesRepository.setFoundDate(date)
    }

    val exchangeRateLiveDate = Transformations.switchMap(triggerForCurrencyExchange) {
        viewModelScope.launch {
            mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()?.getCurrentRate()
        }
        expensesRepository.exchangeRateLiveDate

    }

    // For map (osmdroid)
    private val paramMapViewMutableLiveData = MutableLiveData<Pair<Double, GeoPoint>>()
    val paramMapViewLiveData: LiveData<Pair<Double, GeoPoint>>
        get() = paramMapViewMutableLiveData

    fun setParamMapView(param: Pair<Double, GeoPoint>) {
        paramMapViewMutableLiveData.postValue(param)
    }


    private val filterForExpensesMap = MutableLiveData<FilterForExpensesMap>()
    val expensesForMapMutableLiveData =
        MediatorLiveData<Pair<List<Expenses>, FilterForExpensesMap?>>().apply {
            var lastExpenses = listOf<Expenses>()
            var lastFilterForExpensesMap: FilterForExpensesMap? = null
            addSource(expenses) {
                lastExpenses = it
                postValue(Pair(it, lastFilterForExpensesMap))

            }
            addSource(filterForExpensesMap) { filter ->
                lastFilterForExpensesMap = filter
                when (filter) {
                    is FilterForExpensesMap.All -> postValue(
                        Pair(
                            lastExpenses,
                            lastFilterForExpensesMap
                        )
                    )

                    is FilterForExpensesMap.DateRangeFilter -> {
                        postValue(
                            Pair(
                                lastExpenses.filter { it.dateTime.time in filter.startPeriod..filter.endPeriod },
                                lastFilterForExpensesMap
                            )
                        )
                    }

                    is FilterForExpensesMap.ExpenseFilter ->
                        postValue(
                            Pair(
                                lastExpenses.filter { it.expense == filter.expenseName },
                                lastFilterForExpensesMap
                            )
                        )

                }
            }
        }

    fun setFilterForExpensesMap(filter: FilterForExpensesMap) {
        filterForExpensesMap.postValue(filter)
    }

    fun createJPGFile() = fileSystemRepository.createJPGFile()
    fun deleteFile(file: File) = fileSystemRepository.deleteFile(file)

    fun deleteInvalidFiles(){
        viewModelScope.launch {
            fileSystemRepository.deleteInvalidFiles()
        }
    }
    fun getMediaFiles() = fileSystemRepository.getMediaFiles()

}