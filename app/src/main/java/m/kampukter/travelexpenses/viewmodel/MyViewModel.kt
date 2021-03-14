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

    val savedSettings = expensesRepository.getSettingsLiveData()

    /*
    Begin
    Folders Expenses
   */
    val currentFolder: LiveData<Folders> = expensesRepository.currentFolderFlow.asLiveData()

    private val getAllFolders: LiveData<List<FoldersExtendedView>> =
        expensesRepository.getAllFolders().asLiveData()

    fun saveNewFolder(folder: Folders) {
        viewModelScope.launch {
            expensesRepository.addFolder(folder)
        }
    }

    private val newFolderName = MutableLiveData<String>()
    fun setNewFolderName(name: String) {
        newFolderName.postValue(name)
    }

    private val newFolderDescription = MutableLiveData<String>()
    fun setNewFolderDescription(description: String) {
        newFolderDescription.postValue(description)
    }

    val lastFolderLiveData: LiveData<Pair<Folders, List<FoldersExtendedView>>> =
        MediatorLiveData<Pair<Folders, List<FoldersExtendedView>>>().apply {
            var lastAllFolders = listOf<FoldersExtendedView>()
            var lastFolderName: String? = null
            var lastFolderDescription: String? = null
            fun update() {
                val folderName = lastFolderName ?: return
                postValue(
                    Pair(
                        Folders(shortName = folderName, description = lastFolderDescription),
                        lastAllFolders
                    )
                )
            }
            addSource(getAllFolders) { folders ->
                lastAllFolders = folders
                update()
            }
            addSource(newFolderName) { name ->
                lastFolderName = name
                update()
            }
            addSource(newFolderDescription) { description ->
                lastFolderDescription = description
                update()
            }

        }

    val folderCandidates: LiveData<List<FoldersExtendedView>> =
        MediatorLiveData<List<FoldersExtendedView>>().apply {
            var lastCurrentFolder: Folders? = null
            var lastListFolders = listOf<FoldersExtendedView>()

            fun update() {
                if (lastCurrentFolder != null && lastListFolders.isNotEmpty()) lastCurrentFolder?.let { folder ->
                    postValue(lastListFolders.filter { it.id != folder.id })
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

    private val updateFolderName = MutableLiveData<String>()
    fun setFolderNameForUpd(name: String) {
        updateFolderName.postValue( name)
    }
    private val updateFolderDescription = MutableLiveData<String>()
    fun setFolderDescriptionForUpd(description: String) {
        updateFolderDescription.postValue(description)
    }

    fun updateFolder(folder: Folders) {
        viewModelScope.launch {
            expensesRepository.updateFolder(folder)
        }
    }

    val editFolderLiveData: LiveData<Triple<Folders, Folders, List<FoldersExtendedView>>> =
        MediatorLiveData<Triple<Folders, Folders, List<FoldersExtendedView>>>().apply {
            var lastCandidates = listOf<FoldersExtendedView>()
            var lastNewFolder: Folders? = null
            var lastCurrentFolder: Folders? = null
            fun update() {
                val newFolder = lastNewFolder ?: return
                val currentFolder = lastCurrentFolder ?: return
                postValue(Triple(currentFolder, newFolder, lastCandidates))
            }
           /* addSource(updateFoldersCandidate) {
                if (it != lastNewFolder) {
                    lastNewFolder = it
                    update()
                }
            }*/
            addSource(folderCandidates) { listFolders ->
                lastCandidates = listFolders
                update()
            }
            addSource(currentFolder) {
                lastCurrentFolder = it
                if (lastNewFolder == null) lastNewFolder = it
                update()
            }
            addSource(updateFolderName) { name ->
                if (name != null) {
                    lastNewFolder = lastNewFolder?.copy(shortName = name)
                    update()
                }
            }
            addSource(updateFolderDescription) { description ->
                if ( description != null) {
                    lastNewFolder = lastNewFolder?.copy(description = description)
                    update()
                }
            }
        }

    fun deleteFolderId(folderId: Long) {
        viewModelScope.launch { expensesRepository.deleteFolder(folderId) }
    }


    /*
    End
    Folders Expenses
    */

    /*
    * получение итогов по статьям и по валютам
    */
    fun getExpensesSum(): LiveData<List<ReportSumView>> =
        expensesRepository.getExpensesSum().asLiveData()


    fun getCurrencySum(): LiveData<List<ReportSumView>> =
        expensesRepository.getCurrencySum().asLiveData()

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

    val expensesInFolder: LiveData<Pair<Folders, List<ExpensesExtendedView>>> =
        MediatorLiveData<Pair<Folders, List<ExpensesExtendedView>>>().apply {
            var lastCurrentFolder: Folders? = null
            var lastExpenses: List<ExpensesExtendedView>? = null
            fun update() {
                val currentFolder = lastCurrentFolder ?: return
                val expenses = lastExpenses ?: return
                postValue(Pair(currentFolder, expenses))
            }
            addSource(expensesRepository.getExpensesFlow().asLiveData()) { expenses ->
                lastExpenses = expenses
                update()
            }
            addSource(currentFolder) { folder ->
                lastCurrentFolder = folder
                update()
            }
        }

    val expenses: LiveData<List<Expenses>> = expensesRepository.getAll().asLiveData()

    val expensesWithRate: LiveData<List<ExpensesWithRate>> =
        expensesRepository.getAllExpensesWithRate().asLiveData()


    private val expensesFindId = MutableLiveData<Long>()
    fun setQueryExpensesId(query: Long) {
        expensesFindId.postValue(query)
    }

    val expensesById: LiveData<Expenses> =
        Transformations.switchMap(expensesFindId) { query ->
            expensesRepository.getRecordById(
                query
            )
        }


    fun setDefCurrency(query: String) {
        expensesRepository.setDefCurrency(query)
    }

    private val savedStateSearchFragmentMutableLiveData =
        MutableLiveData<List<ExpensesMainCollection>>()
    val savedStateSearchFragmentLiveData: LiveData<List<ExpensesMainCollection>>
        get() = savedStateSearchFragmentMutableLiveData

    fun setSavedStateSearchFragment(savedValue: List<ExpensesMainCollection>) {
        savedStateSearchFragmentMutableLiveData.postValue(savedValue)
    }

    private val savedStateHomeFragmentMutableLiveData =
        MutableLiveData<List<ExpensesMainCollection>>()
    val savedStateHomeFragmentLiveData: LiveData<List<ExpensesMainCollection>>
        get() = savedStateHomeFragmentMutableLiveData

    fun setSavedStateHomeFragment(savedValue: List<ExpensesMainCollection>) {
        savedStateHomeFragmentMutableLiveData.postValue(savedValue)
    }

    fun deleteSelectedExpenses(selectedIds: Set<Long>) {
        viewModelScope.launch {
            expensesRepository.deleteIdList(selectedIds)
        }
    }

    fun moveSelectedExpenses(selectedIds: Set<Long>, newFolderId: Long) {
        viewModelScope.launch {
            expensesRepository.moveIdList(selectedIds, newFolderId)
        }
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
        Transformations.switchMap(queryExpense) { query ->
            expensesRepository.getExpenseByName(
                query
            )
        }

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
                        } else postValue(
                            ExpenseDeletionResult.Warning(
                                expenseName,
                                numberRec
                            )
                        )
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
    val expenseUpdateMediator: LiveData<Int> = MediatorLiveData<Int>().apply {
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

    fun setSettingNewFolder(folderId: Long) {
        expensesRepository.updateFolderInSettings(folderId)
    }

    val savedSettingsLiveData: LiveData<Settings> = MediatorLiveData<Settings>().apply {
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


    val expenseMediatorLiveData: LiveData<Pair<Expenses?, List<CurrencyTable>?>> =
        MediatorLiveData<Pair<Expenses?, List<CurrencyTable>?>>().apply {
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
    val bufferExpensesMediatorLiveData: LiveData<Pair<Expenses?, List<CurrencyTable>?>> =
        MediatorLiveData<Pair<Expenses?, List<CurrencyTable>?>>().apply {
            var lastExpenses: Expenses? = null
            var currencyList: List<CurrencyTable> = emptyList()
            var lastCurrentFolder = 0L
            addSource(bufferForSaveExpense) {
                lastExpenses = it
                postValue(Pair(lastExpenses, currencyList))
               /* isSavingAllowed.postValue(
                    it != null && it.currency.isNotBlank() && (it.sum != 0.0) && it.note.isNotBlank() && it.expense.isNotBlank()
                )*/
            }
            addSource(currencyTableList) {
                if (it != null) currencyList = it
                postValue(Pair(lastExpenses, currencyList))
            }
            addSource(isSaveNewExpenses) {

                if (it) lastExpenses?.let { _expenses ->
                    viewModelScope.launch {
                        expensesRepository.addExpenses(_expenses.copy(folder_id = lastCurrentFolder))

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
            addSource(currentFolder) {
                lastCurrentFolder = it.id
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
    val exchangeRateLiveDate: LiveData<ResultCurrentExchangeRate> =
        MediatorLiveData<ResultCurrentExchangeRate>().apply {
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
    val expensesForMapMutableLiveData: LiveData<Pair<List<Expenses>, FilterForExpensesMap?>> =
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

                    is FilterForExpensesMap.ExpenseFilter ->{}
                       /* postValue(
                            Pair(
                                lastExpenses.filter { it.expense == filter.expenseName },
                                lastFilterForExpensesMap
                            )
                        )*/

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

    private val expensesSearch: LiveData<Pair<String, String>> =
        MediatorLiveData<Pair<String, String>>().apply {
            var lastSearchStringExpenses: String? = null
            var lastCurrentFolderName: String? = null
            fun update() {
                lastSearchStringExpenses?.let { searchString ->
                    lastCurrentFolderName?.let { folder ->
                        postValue(Pair(searchString, folder))
                    }
                }
            }
            addSource(searchStringExpenses) {
                if (it != null) {
                    lastSearchStringExpenses = it
                    update()
                }
            }
            addSource(currentFolder) {
                if (it != null) {
                    lastCurrentFolderName = it.shortName
                    update()
                }
            }
        }
    val expensesSearchResult = Transformations.switchMap(expensesSearch) { (query, folder) ->
        expensesRepository.getSearchExpenses( query, folder)
            .asLiveData()
    }
    val searchStringExpensesLiveData: LiveData<String>
        get() = searchStringExpenses

    fun setSearchStringExpenses(lastSearchString: String) {
        searchStringExpenses.postValue(lastSearchString)
    }

    fun getHistorySearchStringExpenses() =
        expensesRepository.getHistorySearchStringExpenses()

    private var isSearchResultExpensesActive = false
    fun setSearchResultExpensesOpenActive(value: Boolean) {
        isSearchResultExpensesActive = value
    }

    fun getSearchResultExpensesOpenActive() = isSearchResultExpensesActive


}

