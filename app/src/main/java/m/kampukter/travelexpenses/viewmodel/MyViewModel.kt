package m.kampukter.travelexpenses.viewmodel

import android.util.Log
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

    val savedSettings = expensesRepository.getSettingsLiveData()

    /*
    Begin
    Folders Expenses
   */

    private val getAllFolders = expensesRepository.getAllFolders()
    val currentFolder = Transformations.switchMap(savedSettings) {
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            if (it != null) emitSource(expensesRepository.searchFolderById(it.folder))
        }
    }
    private val isSaveNewFolder = MutableLiveData<Boolean>()
    fun saveNewFolder() {
        isSaveNewFolder.postValue(true)
    }

    private val newFolderName = MutableLiveData<String>()
    fun setNewFolderName(name: String) {
        newFolderName.postValue(name)
    }

    private val newFolderDescription = MutableLiveData<String>()
    fun setNewFolderDescription(description: String) {
        newFolderDescription.postValue(description)
    }

    private val inputShortNameErrorMutableLiveData = MutableLiveData<String>()
    val inputShortNameError: LiveData<String>
        get() = inputShortNameErrorMutableLiveData

    val isFolderSavingAllowed = MediatorLiveData<Boolean>().apply {
        var lastNewFolder = Folders("", null)
        var lastListName = listOf<String>()
        fun update() {
            val isFoldersIsCorrect = !lastListName.contains(lastNewFolder.shortName) &&
                    lastNewFolder.shortName.isNotBlank()
            postValue(isFoldersIsCorrect)
            inputShortNameErrorMutableLiveData.postValue(
                when {
                    lastNewFolder.shortName.isBlank() -> "Заполните поле"
                    lastListName.contains(lastNewFolder.shortName) -> "Такое имя уже есть"
                    else -> null
                }
            )

        }
        addSource(getAllFolders) { listFolders ->
            lastListName = listFolders.map { it.shortName }
            update()
        }
        //Add new
        addSource(isSaveNewFolder) {
            val isFoldersIsCorrect = !lastListName.contains(lastNewFolder.shortName) &&
                    lastNewFolder.shortName.isNotBlank()
            if (it && isFoldersIsCorrect) viewModelScope.launch {
                lastListName.contains(lastNewFolder.shortName)
                expensesRepository.addFolder(lastNewFolder)
            }
        }

        addSource(newFolderName) {
            if (it != null) {
                lastNewFolder = lastNewFolder.copy(shortName = it)
                update()
            }
        }
        addSource(newFolderDescription) {
            if (it != null) {
                lastNewFolder = lastNewFolder.copy(description = it)
                update()
            }
        }
    }


    val folderCandidates = MediatorLiveData<List<FoldersExtendedView>>().apply {
        var lastCurrentFolder: FoldersExtendedView? = null
        var lastListFolders = listOf<FoldersExtendedView>()

        fun update() {
            if (lastCurrentFolder != null && lastListFolders.isNotEmpty()) lastCurrentFolder?.let { folder ->
                postValue(lastListFolders.filter { it.shortName != folder.shortName })
            }
        }

        addSource(getAllFolders) {
            if (it != null) {
                lastListFolders = it
                update()
            }
        }
        addSource(currentFolder) {
            if (it != null) {
                lastCurrentFolder = it
                update()
            }
        }

    }

    private val editFolderName = MutableLiveData<String>()
    fun setEditFolderName(name: String) {
        editFolderName.postValue(name)
    }

    private val editFolderDescription = MutableLiveData<String>()
    fun setEditFolderDescription(description: String) {
        editFolderDescription.postValue(description)
    }

    private val updateFolderTrigger = MutableLiveData<Boolean>()
    fun setUpdateFolderTrigger() {
        updateFolderTrigger.postValue(true)
    }

    val editFolderErrorMsg = MediatorLiveData<String?>().apply {

        var lastListName = listOf<String>()
        var lastCurrentFolder: FoldersExtendedView? = null
        var oldFolderName: String? = null

        fun update() {
            lastCurrentFolder?.let { folderName ->
                postValue(
                    when {
                        folderName.shortName.isBlank() -> "Заполните поле"
                        lastListName.contains(folderName.shortName) -> "Такое имя уже есть"
                        else -> null
                    }
                )
            }
        }
        addSource(folderCandidates) { listFolders ->
            lastListName = listFolders.map { it.shortName }
            update()
        }

        addSource(currentFolder) {
            if (it != null) {
                lastCurrentFolder = it
                oldFolderName = it.shortName
            }
        }
        addSource(editFolderName) {
            if (it != null) {
                lastCurrentFolder = lastCurrentFolder?.copy(shortName = it)
                update()
            }
        }
        addSource(editFolderDescription) {
            if (it != null) {
                lastCurrentFolder = lastCurrentFolder?.copy(description = it)
                lastCurrentFolder?.let { folderExtendedView ->
                    val folder =
                        Folders(folderExtendedView.shortName, folderExtendedView.description)
                    viewModelScope.launch { expensesRepository.addFolder(folder) }
                }
            }
        }
        addSource(updateFolderTrigger) {
            oldFolderName?.let { oldName ->
                lastCurrentFolder?.let { folderName ->
                    val isCorrect = when {
                        folderName.shortName.isBlank() -> false
                        lastListName.contains(folderName.shortName) -> false
                        else -> true
                    }
                    if (isCorrect) viewModelScope.launch {
                        expensesRepository.updateFolder(folderName.shortName, oldName)
                    }
                }
            }
        }
    }

    private val folderDelTrigger = MutableLiveData<Boolean>()
    fun deleteFolderName(folderName: String) {
        folderDeleteName.postValue(folderName)
    }

    private val folderDeleteName = MutableLiveData<String>()
    fun deleteFolderTrigger(isDelete: Boolean) {
        folderDelTrigger.postValue(isDelete)
    }

    val folderDeletionResult: LiveData<FolderDeletionResult> =
        MediatorLiveData<FolderDeletionResult>().apply {
            var lastFolderName: String? = null
            var lastFolderTrigger: Boolean? = null
            fun reset() {
                folderDelTrigger.postValue(null)
                folderDeleteName.postValue(null)
                postValue(null)
            }

            fun update() {
                val folderName = lastFolderName
                val folderTrigger = lastFolderTrigger

                if (folderName == null || folderTrigger == null) return

                viewModelScope.launch {
                    if (folderTrigger) {
                        expensesRepository.deleteFolder(folderName, true)
                        postValue(FolderDeletionResult.Success)
                        delay(1000)
                        reset()
                    } else {
                        val numberRec = expensesRepository.deleteFolder(
                            folderName,
                            folderTrigger
                        )
                        if (numberRec == 0L) {
                            expensesRepository.deleteFolder(folderName, true)
                            postValue(FolderDeletionResult.Success)
                            delay(1000)
                            reset()
                        } else postValue(FolderDeletionResult.Warning(folderName, numberRec))
                    }
                }
            }
            addSource(folderDeleteName) {
                lastFolderName = it
                update()
            }
            addSource(folderDelTrigger) {
                lastFolderTrigger = it
                update()
            }
        }
    /*
    End
    Folders Expenses
    */

    /*
    * получение итогов по статьям и по валютам
    */
    fun getExpensesSum() = Transformations.switchMap(currentFolder) { folder ->
        expensesRepository.getExpensesSum(folder.shortName)
    }

    fun getCurrencySum() = Transformations.switchMap(currentFolder) { folder ->
        expensesRepository.getCurrencySum(folder.shortName)
    }

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


    val expenses: LiveData<List<Expenses>> =
        Transformations.switchMap(currentFolder) { folder -> expensesRepository.getAll(folder.shortName) }
    val expensesWithRate = Transformations.switchMap(currentFolder) { folder ->
        expensesRepository.getAllExpensesWithRate(folder.shortName)
    }


    private val expensesFindId = MutableLiveData<Long>()
    fun setQueryExpensesId(query: Long) {
        expensesFindId.postValue(query)
    }

    val expensesById: LiveData<Expenses> =
        Transformations.switchMap(expensesFindId) { query -> expensesRepository.getRecordById(query) }


    fun setDefCurrency(query: String) {
        expensesRepository.setDefCurrency(query)
    }

    private val selectedExpensesMutableLiveData = MutableLiveData<List<ExpensesMainCollection>>()
    val selectedExpensesLiveData: LiveData<List<ExpensesMainCollection>>
        get() = selectedExpensesMutableLiveData

    fun setSelectedExpenses(selectedExpenses: List<ExpensesMainCollection>) {
        selectedExpensesMutableLiveData.postValue(selectedExpenses)
    }

    private val expensesDeleteTrigger = MutableLiveData<Boolean>()
    val expensesDeleteStatusMediatorLiveData = MediatorLiveData<Boolean>().apply {
        var lastSelectedExpenses = setOf<Long>()
        addSource(expensesDeleteTrigger) {
            if (it) {
                if (lastSelectedExpenses.isNotEmpty())
                    viewModelScope.launch {
                        expensesRepository.deleteIdList(lastSelectedExpenses)
                    }
                postValue(true)
                selectedExpensesMutableLiveData.postValue(listOf())
            }

        }
        addSource(selectedExpensesMutableLiveData) { selectedExpenses ->
            if (selectedExpenses != null)
                lastSelectedExpenses =
                    selectedExpenses.map { (it as ExpensesMainCollection.Row).id }.toSet()
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

    private val settingStatusGPS = MutableLiveData<Int>()
    fun setSettingStatusGPS(status: Int) {
        settingStatusGPS.postValue(status)
    }

    fun setSettingNewFolder(folder: String) {
        viewModelScope.launch {
            val settings = expensesRepository.getSettings()
            if (settings != null) expensesRepository.insertSettings(settings.copy(folder = folder))
        }
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

    val expensesInFolder =
        Transformations.switchMap(currentFolder) { folder -> expensesRepository.getExpenses(folder.shortName) }

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
            var lastCurrentFolder: String? = null
            addSource(bufferForSaveExpense) {
                lastExpenses = it
                postValue(Pair(lastExpenses, currencyList))
                isSavingAllowed.postValue(
                    it != null && it.currency.isNotBlank() && (it.sum != 0.0) && it.note.isNotBlank() && it.expense.isNotBlank()
                )
            }
            addSource(currencyTableList) {
                if (it != null) currencyList = it
                postValue(Pair(lastExpenses, currencyList))
            }
            addSource(isSaveNewExpenses) {

                if (it) lastExpenses?.let { _expenses ->
                    lastCurrentFolder?.let { folder ->
                        viewModelScope.launch {
                            expensesRepository.addExpenses(_expenses.copy(folder = folder))

                            //установка сохраняемой валюты как по умолчанию
                            expensesRepository.resetDef()
                            expensesRepository.setDefCurrency(_expenses.currency)

                            isSaveNewExpenses.postValue(false)
                        }
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
            addSource(currentFolder) {
                if (it != null) lastCurrentFolder = it.shortName

            }
        }

    fun setBufferExpenses(expenses: Expenses?) {
        bufferForSaveExpense.postValue(expenses)
    }

    /*
    Exchange
    */

    private val queryInExchangeCurrentRate = MutableLiveData<String>()
    val queryInExchangeLiveData: LiveData<String>
        get() = queryInExchangeCurrentRate

    fun setQueryInExchangeCurrentRate(query: String) {
        queryInExchangeCurrentRate.postValue(query)
    }

    private val triggerForCurrencyExchange = MutableLiveData<Boolean>()
    fun getExchangeCurrency() {
        triggerForCurrencyExchange.postValue(true)
    }

    private val foundDateExchangeCurrency = MutableLiveData<Date>()
    fun setDateForCurrencyExchange(date: Date) {
        foundDateExchangeCurrency.postValue(date)
    }

    private val resultExchangeCurrentRateLiveData = expensesRepository.exchangeRateLiveDate
    val exchangeRateLiveDate = MediatorLiveData<ResultCurrentExchangeRate>().apply {
        var lastResultExchangeCurrentRate: ResultCurrentExchangeRate? = null
        var lastQuery: String? = null
        var lastFoundDate: Date? = null

        fun filterExchangeCollection(query: String): List<ExchangeCurrentRate>? {
            return (lastResultExchangeCurrentRate as? ResultCurrentExchangeRate.Success)?.exchangeCurrentRate?.filter { item ->
                item.currencyName.indexOf(
                    query,
                    0,
                    true
                ) != -1 || item.currencyCode.indexOf(query, 0, true) != -1
            }
        }

        fun getExchangeCurrency() {
            viewModelScope.launch {
                mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()
                    ?.getCurrentRate()
            }
        }

        addSource(triggerForCurrencyExchange) {
            if (lastFoundDate == null) getExchangeCurrency()
        }
        addSource(foundDateExchangeCurrency) {
            expensesRepository.setFindDate(it)
            if (lastFoundDate != it) getExchangeCurrency()
            lastFoundDate = it

        }
        addSource(resultExchangeCurrentRateLiveData) { result ->
            lastResultExchangeCurrentRate = result
            if (result is ResultCurrentExchangeRate.ErrorAPI) lastFoundDate = null
            if (lastQuery.isNullOrEmpty()) postValue(result)
            else {
                lastQuery?.let {
                    filterExchangeCollection(it)?.let { filter ->
                        postValue(
                            ResultCurrentExchangeRate.Success(filter)
                        )
                    }
                }
            }
        }
        addSource(queryInExchangeCurrentRate) { query ->
            lastQuery = query
            if (query.isNotEmpty()) {
                query?.let { filterExchangeCollection(it) }?.let { filter ->
                    postValue(
                        ResultCurrentExchangeRate.Success(filter)
                    )
                }
            } else postValue(lastResultExchangeCurrentRate)
        }
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

    fun deleteInvalidFiles() {
        viewModelScope.launch {
            fileSystemRepository.deleteInvalidFiles()
        }
    }

/*
Search in Expenses
*/

    private val searchStringExpenses = MutableLiveData<String>().apply { postValue(null) }
    val expensesSearchResult = Transformations.switchMap(searchStringExpenses) { searchString ->
        expensesRepository.getSearchExpensesWithRate(searchString)
    }
    val searchStringExpensesLiveData: LiveData<String>
        get() = searchStringExpenses

    fun setSearchStringExpenses(lastSearchString: String) {
        searchStringExpenses.postValue(lastSearchString)
    }

    fun getHistorySearchStringExpenses() = expensesRepository.getHistorySearchStringExpenses()

    private var isSearchResultExpensesActive = false
    fun setSearchResultExpensesOpenActive(value: Boolean) {
        isSearchResultExpensesActive = value
    }

    fun getSearchResultExpensesOpenActive() = isSearchResultExpensesActive


}

