package m.kampukter.travelexpenses.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
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

    private val newExpensesExpense = MutableLiveData<Long>()
    fun setLastExpense(id: Long) {
        newExpensesExpense.postValue( id )
    }

    private val newExpensesSum = MutableLiveData<Double>()
    fun setLastSum(sum: Double) {
        newExpensesSum.postValue(sum)
    }

    private val newExpensesNote = MutableLiveData<String>()
    fun setLastNote(note: String) {
        newExpensesNote.postValue( note )
    }
    private val newExpensesCurrency = MutableLiveData<String>()
    fun setLastCurrency(currency: String) {
        newExpensesCurrency.postValue( currency )
    }

    private val newExpensesLocation = MutableLiveData<MyLocation?>()
    fun setLastLocation(location: MyLocation) {
        newExpensesLocation.postValue( location )
    }

    private val newExpensesImageURI = MutableLiveData<String?>()
    fun setLastUriPhoto(imageUri: String?) {
        newExpensesImageURI.postValue( imageUri )
    }

    private val isAddNewExpense = MutableLiveData<Boolean>()
    fun addNewExpenses() = isAddNewExpense.postValue(true)

    val addExpensesLiveData: LiveData<Triple<ExpensesExtendedView, List<Expense>, List<CurrencyTable>>> =
        MediatorLiveData<Triple<ExpensesExtendedView, List<Expense>, List<CurrencyTable>>>().apply {
            var lastExpenses = ExpensesExtendedView()
            var lastExpenseList: List<Expense> = listOf()
            var lastCurrencyList: List<CurrencyTable> = listOf()
            var lastCurrentFolder: Folders? = null
            fun update() {
                postValue(Triple(lastExpenses, lastExpenseList, lastCurrencyList))
            }
            addSource(isAddNewExpense) {
                lastCurrentFolder?.let {
                    lastExpenses = lastExpenses.copy(folderId = it.id)
                    viewModelScope.launch {
                        expensesRepository.addExpenses(lastExpenses)
                    }
                }
            }
            addSource(expenseList) {
                lastExpenseList = it
                update()
            }

            addSource(currentFolder) {
                lastCurrentFolder = it
            }
            addSource(currencyTableList) {
                lastCurrencyList = it
                update()
            }
            addSource(newExpensesImageURI){
                lastExpenses = lastExpenses.copy(imageUri = it)
                update()
            }
            addSource(newExpensesLocation){
                lastExpenses = lastExpenses.copy(location = it)
                update()
            }
            addSource(newExpensesNote){
                lastExpenses = lastExpenses.copy(note = it)
                update()
            }
            addSource(newExpensesSum){
                lastExpenses = lastExpenses.copy(sum = it)
                update()
            }
            addSource(newExpensesExpense){
                lastExpenses = lastExpenses.copy( expense_id = it)
                update()
            }
            addSource(newExpensesCurrency){
                lastExpenses = lastExpenses.copy(currency = it)
                update()
            }
        }

    // Edit record Expenses
    private val expensesIdEditMutableLiveData = MutableLiveData<Long>()
    val expensesIdEditLiveData: LiveData<Long>
        get() = expensesIdEditMutableLiveData

    fun expensesIdEdit(expensesId: Long) {
        expensesIdEditMutableLiveData.postValue(expensesId)
    }

    private val expensesById =
        Transformations.switchMap(expensesIdEditMutableLiveData) { expensesId ->
            expensesRepository.getExpensesById(expensesId).asLiveData()
        }

    fun updateExpenses(value: EditedExpensesField) {
        viewModelScope.launch {
            when (value) {
                is EditedExpensesField.NoteField ->
                    expensesRepository.updateNote(value.idExpenses, value.note)
                is EditedExpensesField.ExpenseField ->
                    expensesRepository.updateExpense(value.idExpenses, value.expense.id)
                is EditedExpensesField.CurrencyField ->
                    expensesRepository.updateCurrency(value.idExpenses, value.currency.name)
                is EditedExpensesField.SumField ->
                    expensesRepository.updateSum(value.idExpenses, value.sum)
                is EditedExpensesField.ImageUriField ->
                    expensesRepository.updateImageUri(value.idExpenses, value.uri)
            }
        }
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
    fun setEditExpenseName(expenseName: String) {
        editExpenseName.postValue(expenseName)
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
            addSource(editExpenseName) { name ->
                lastEditExpense = lastEditExpense?.copy(name = name)
                lastEditExpense?.let {
                    expensesRepository.updateExpense(
                        it.id,
                        it.name
                    )
                }
            }
        }

    fun addNewExpense(expense: Expense) {
        expensesRepository.addExpense(expense)
    }

    fun deleteExpense(idExpense: Long) {
        expensesRepository.deleteExpense(idExpense)
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
    fun setQueryInExchangeCurrentRate(query: String) {
        queryInExchangeCurrentRate.postValue(query)
    }

    private val foundDateExchangeCurrency = MutableLiveData<Date>().apply {
        postValue(Calendar.getInstance().time)
    }

    fun setDateForCurrencyExchange(date: Date) {
        foundDateExchangeCurrency.postValue(date)
    }

    private val _rate: LiveData<ResultCurrentExchangeRate> =
        foundDateExchangeCurrency.distinctUntilChanged().switchMap { date ->
            liveData {
                mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()
                    ?.fetchRate(date)?.collect { emit(it) }
            }
        }
    val currencyRateLiveDate: LiveData<Pair<ResultCurrentExchangeRate, String?>> =
        MediatorLiveData<Pair<ResultCurrentExchangeRate, String?>>().apply {

            var lastResultExchangeCurrentRate: ResultCurrentExchangeRate? = null
            var lastQuery: String? = null

            fun update() {
                val resultExchangeCurrentRate = lastResultExchangeCurrentRate ?: return
                postValue(Pair(resultExchangeCurrentRate, lastQuery))
            }

            addSource(_rate) {
                lastResultExchangeCurrentRate = it
                update()
            }
            addSource(queryInExchangeCurrentRate) { query ->
                lastQuery = query
                update()
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
            fun update() {
                postValue(Pair(lastExpenses, lastFilterForExpensesMap))
            }
            addSource(expensesRepository.getExpensesFlow().asLiveData()) {
                lastExpenses = it
                update()
            }
            addSource(filterForExpensesMap) { filter ->
                lastFilterForExpensesMap = filter
                update()
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
            expensesRepository.getSearchExpenses(query)
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

