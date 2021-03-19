package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
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
        updateFolderName.postValue(name)
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
                if (description != null) {
                    lastNewFolder = lastNewFolder?.copy(description = description)
                    update()
                }
            }
        }

    fun deleteFolderId(folderId: Long) {
        viewModelScope.launch { expensesRepository.deleteFolder(folderId) }
    }
    /*
    End Folders Expenses
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

    fun deleteAllExpenses() {
        viewModelScope.launch { expensesRepository.deleteAll() }
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

    private val currencyTableList: LiveData<List<CurrencyTable>> =
        expensesRepository.getCurrencyAllFlow().asLiveData()

    val expenseList: LiveData<List<Expense>> = expensesRepository.getAllExpenseFlow().asLiveData()

    private val lastExpenseMutableLiveData = MutableLiveData<Long>()
    fun setLastExpense(id: Long) {
        lastExpenseMutableLiveData.postValue(id)
    }

    private val lastSumMutableLiveData = MutableLiveData<Double>()
    fun setLastSum(sum: Double) {
        lastSumMutableLiveData.postValue(sum)
    }

    private val lastNoteMutableLiveData = MutableLiveData<String>()
    fun setLastNote(note: String) {
        lastNoteMutableLiveData.postValue(note)
    }

    private val lastCurrencyMutableLiveData = MutableLiveData<String>()
    fun setLastCurrency(note: String) {
        lastCurrencyMutableLiveData.postValue(note)
    }

    private val lastLocationMutableLiveData = MutableLiveData<MyLocation>()
    fun setLastLocation(location: MyLocation) {
        lastLocationMutableLiveData.postValue(location)
    }

    private val lastUriPhotoMutableLiveData = MutableLiveData<String?>()
    fun setLastUriPhoto(uriPhoto: String?) {
        lastUriPhotoMutableLiveData.postValue(uriPhoto)
    }

    private val isAddNewExpense = MutableLiveData<Boolean>()
    fun addNewExpenses() = isAddNewExpense.postValue(true)

    val addExpensesLiveData: LiveData<Triple<ExpensesUpdate, List<Expense>, List<CurrencyTable>>> =
        MediatorLiveData<Triple<ExpensesUpdate, List<Expense>, List<CurrencyTable>>>().apply {
            var lastExpenses = ExpensesUpdate()
            var lastExpenseList: List<Expense> = listOf()
            var lastCurrencyList: List<CurrencyTable> = listOf()
            fun update() {
                postValue(Triple(lastExpenses, lastExpenseList, lastCurrencyList))
            }
            addSource(isAddNewExpense) {
                viewModelScope.launch {
                    expensesRepository.addExpenses(lastExpenses)
                }
            }
            addSource(expenseList) {
                lastExpenseList = it
                update()
            }
            addSource(currentFolder) {
                lastExpenses = lastExpenses.copy(folderId = it.id)
            }
            addSource(currencyTableList) {
                lastCurrencyList = it
                update()
            }
            addSource(lastExpenseMutableLiveData) { id ->
                lastExpenses = lastExpenses.copy(expense_id = id)
                update()
            }
            addSource(lastSumMutableLiveData) { sum ->
                lastExpenses = lastExpenses.copy(sum = sum)
                update()
            }
            addSource(lastNoteMutableLiveData) { note ->
                lastExpenses = lastExpenses.copy(note = note)
                update()
            }
            addSource(lastCurrencyMutableLiveData) {
                lastExpenses = lastExpenses.copy(currency = it)
                update()
            }
            addSource(lastLocationMutableLiveData) {
                lastExpenses = lastExpenses.copy(location = it)
                update()
            }
            addSource(lastUriPhotoMutableLiveData) {
                lastExpenses = lastExpenses.copy(imageUri = it)
                update()
            }
        }

    // Edit record Expenses
    private val expensesIdEditMutableLiveData = MutableLiveData<Long>()
    fun expensesIdEdit(expensesId: Long) {
        expensesIdEditMutableLiveData.postValue(expensesId)
    }

    private val expensesById =
        Transformations.switchMap(expensesIdEditMutableLiveData) { expensesId ->
            expensesRepository.getExpensesById(expensesId).asLiveData()
        }
    private val updateExpensesMutableLiveData = MutableLiveData<Expense>()
    private val updateExpensesCurrency = MutableLiveData<CurrencyTable>()
    private val updateExpensesNote = MutableLiveData<String>()
    private val updateExpensesSum = MutableLiveData<Double>()
    private val updateExpensesImageUri = MutableLiveData<String?>()
    fun updateExpenses(expense: Expense) {
        updateExpensesMutableLiveData.postValue(expense)
    }

    fun updateExpenses(currency: CurrencyTable) {
        updateExpensesCurrency.postValue(currency)
    }

    fun updateExpenses(note: String) {
        updateExpensesNote.postValue(note)
    }

    fun updateExpenses(sum: Double) {
        updateExpensesSum.postValue(sum)
    }

    fun updateExpensesImageUri(value: String?) {
        updateExpensesImageUri.postValue(value)
    }

    val expensesEdit: LiveData<Pair<ExpensesExtendedView, List<CurrencyTable>>> =
        MediatorLiveData<Pair<ExpensesExtendedView, List<CurrencyTable>>>().apply {
            var lastExpenses: ExpensesExtendedView? = null
            var lastCurrencyList: List<CurrencyTable> = listOf()
            fun update() {
                val expenses = lastExpenses ?: return
                postValue(Pair(expenses, lastCurrencyList))
            }
            addSource(expensesById) {
                lastExpenses = it
                update()
            }
            addSource(currencyTableList) {
                lastCurrencyList = it
                update()
            }
            addSource(updateExpensesMutableLiveData) { expense ->
                lastExpenses?.let {
                    viewModelScope.launch {
                        expensesRepository.updateExpense(it.id, expense.id)
                    }
                }
            }
            addSource(updateExpensesCurrency) { currency ->
                lastExpenses?.let {
                    viewModelScope.launch {
                        expensesRepository.updateCurrency(it.id, currency.name)
                    }
                }
            }
            addSource(updateExpensesNote) { noteString ->
                noteString?.let { note ->
                    lastExpenses?.let {
                        viewModelScope.launch {
                            expensesRepository.updateNote(it.id, note)
                        }
                    }
                }
            }
            addSource(updateExpensesSum) { sum ->
                lastExpenses?.let {
                    viewModelScope.launch {
                        expensesRepository.updateSum(it.id, sum)
                    }
                }
            }
            addSource(updateExpensesImageUri) { imageUri ->
                lastExpenses?.let {
                    viewModelScope.launch {
                        expensesRepository.updateImageUri(it.id, imageUri)
                    }
                }

            }
        }

    fun deleteImageFromExpenses(id: Long) {
        viewModelScope.launch {
            expensesRepository.updateImageUri(id, null)
        }
    }

    /*
    Edit expense name
    */
    private val editExpense = MutableLiveData<Expense>()
    private val expensesByExpense = Transformations.switchMap(editExpense) {
        expensesRepository.getExpensesByExpense(it.id).asLiveData()
    }
    fun setEditExpense(expense: Expense) {
        editExpense.postValue(expense)
    }
    private val editExpenseName = MutableLiveData<String>()
    fun setEditExpenseName( expenseName: String ){
        editExpenseName.postValue( expenseName)
    }
    val editExpenseLiveData: LiveData<Pair<Expense, List<ExpensesExtendedView>>> =
        MediatorLiveData<Pair<Expense, List<ExpensesExtendedView>>>().apply {
            var lastEditExpense: Expense? = null
            var lastExpensesList: List<ExpensesExtendedView> = listOf()
            fun update() {
                val editExpense = lastEditExpense ?: return
                postValue(Pair(editExpense, lastExpensesList))
            }
            addSource(editExpense) {
                lastEditExpense = it
                update()
            }
            addSource(expensesByExpense) {
                lastExpensesList = it
                update()
            }
            addSource(editExpenseName){ name ->
                lastEditExpense = lastEditExpense?.copy( name = name)
                lastEditExpense?.let { expensesRepository.updateExpense(
                    it.id,
                    it.name
                ) }
            }
        }

    fun addNewExpense(expense: Expense) {
        expensesRepository.addExpense(expense)
    }

    fun deleteExpense(idExpense: Long) {
        expensesRepository.deleteExpense( idExpense )
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

    private val resultExchangeCurrentRateLiveData =
        expensesRepository.exchangeRateLiveDate
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
    fun setFilterForExpensesMap(filter: FilterForExpensesMap) {
        filterForExpensesMap.postValue(filter)
    }

    val expensesInFolderForMap: LiveData<Pair<List<ExpensesExtendedView>, FilterForExpensesMap?>> =
        MediatorLiveData<Pair<List<ExpensesExtendedView>, FilterForExpensesMap?>>().apply {
            var lastFilterForExpensesMap: FilterForExpensesMap? = null
            var lastExpenses = listOf<ExpensesExtendedView>()
            addSource(expensesRepository.getExpensesFlow().asLiveData()) {
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

                    is FilterForExpensesMap.ExpenseFilter -> {
                        postValue(
                            Pair(
                                lastExpenses.filter { it.expense_id == filter.expense.id },
                                lastFilterForExpensesMap
                            )
                        )
                    }
                }
            }
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

    private val searchStringExpenses =
        MutableLiveData<String>().apply { postValue(null) }

    val expensesSearchResult =
        Transformations.switchMap(searchStringExpenses) { query ->
            expensesRepository.getSearchExpenses( query )
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

