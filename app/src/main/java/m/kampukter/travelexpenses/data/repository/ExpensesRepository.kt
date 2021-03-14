package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dao.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.mainApplication
import m.kampukter.travelexpenses.workers.BackupWorker
import java.util.*


class ExpensesRepository(
    private val expensesDao: ExpensesDao,
    private val rateCurrencyDao: RateCurrencyDao,
    private val currencyDao: CurrencyDao,
    private val settingsDao: SettingsDao,
    private val expenseDao: ExpenseDao,
    private val foldersDao: FoldersDao,
    private val backupServer: BackupServer
) {

    private var idWorkRequest: UUID? = null

    private val currentSettingsFlow: Flow<Settings> = settingsDao.getAllSettings()

    val currentFolderFlow: Flow<Folders> = currentSettingsFlow.flatMapLatest { value ->
        foldersDao.searchById(value.folder_id)
    }

    fun getExpensesFlow(): Flow<List<ExpensesExtendedView>> = currentSettingsFlow.flatMapLatest {
        expensesDao.getExpensesView(it.folder_id)
    }
    /*fun getExpensesFlow(): Flow<List<ExpensesWithRate>> = currentSettingsFlow.flatMapLatest {
        expensesDao.getExpensesFlow(it.folder_id)
    }*/

    fun getAll(): Flow<List<Expenses>> =
        currentSettingsFlow.flatMapLatest { expensesDao.getAll(it.folder_id) }

    fun getAllExpensesWithRate(): Flow<List<ExpensesWithRate>> =
        currentSettingsFlow.flatMapLatest { expensesDao.getAllExpensesWithRate(it.folder_id) }


    fun getRecordById(id: Long): LiveData<Expenses> = expensesDao.getExpensesById(id)

    suspend fun addExpenses(expenses: Expenses) {
        expensesDao.insert(expenses)
    }


    suspend fun deleteExpensesById(selectedId: Long) {
        expensesDao.deleteExpensesById(selectedId)
    }

    suspend fun deleteIdList(selectedListId: Set<Long>) {
        expensesDao.deleteIdList(selectedListId)
    }

    suspend fun moveIdList(selectedListId: Set<Long>, newFolderId: Long) {
        expensesDao.moveIdList(selectedListId, newFolderId)
    }

    suspend fun deleteAll() {
        expensesDao.deleteAll()
    }

    suspend fun getAllForSend(): LiveData<String> {
        var resultString = ""
        val result = MutableLiveData<String>()
        expensesDao.getAllExpenses().forEach {
            resultString = resultString + DateFormat.format(
                "dd/MM/yyyy HH:mm",
                it.dateTime
            ) + "," + it.expense_id + "," + it.sum + "," + it.currency + "," + it.note + "\n"
        }
        result.postValue(resultString)
        return result
    }

    fun getExpensesSum(): Flow<List<ReportSumView>> = currentSettingsFlow.flatMapLatest {
        expensesDao.getSumExpenses(it.folder_id)
    }

    fun getCurrencySum(): Flow<List<ReportSumView>> = currentSettingsFlow.flatMapLatest {
        expensesDao.getSumCurrency(it.folder_id)
    }

    // CurrencyDao
    fun getCurrencyAllLiveData(): LiveData<List<CurrencyTable>> = currencyDao.getAllLiveData()

    fun setDefCurrency(currencyName: String) {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.setDefault(currencyName)
        }
    }

    fun resetDef() {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.resetDef()
        }
    }

    suspend fun deleteRate() {
        rateCurrencyDao.deleteAll()
    }

    // SettingsDao
    suspend fun getSettings() = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) {
        settingsDao.insert(settings)
        //getCurrentFolder()
    }

    fun updateFolderInSettings(folderId: Long) =
        GlobalScope.launch(context = Dispatchers.IO) {
            settingsDao.updateFolderId(settingsDao.getUserName(), folderId)
        }

    fun getSettingsLiveData() = settingsDao.getSettingsLiveData()

    /*
    Start Worker for make Backup
     */
    fun startBackupWorker(periodic: Periodic) {

        stopBackupWorker()

        val myWorkRequest = PeriodicWorkRequest.Builder(
            BackupWorker::class.java,
            periodic.value,
            periodic.timeUnit
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(mainApplication)
            .enqueueUniquePeriodicWork(
                "Backup",
                ExistingPeriodicWorkPolicy.REPLACE,
                myWorkRequest
            )
        idWorkRequest = myWorkRequest.id
    }

    /*
    Stop Backup Worker
    */
    fun stopBackupWorker() {
        idWorkRequest?.let { WorkManager.getInstance(mainApplication).cancelWorkById(it) }
    }


    fun saveBackup() {
        GlobalScope.launch {
            val settings = settingsDao.getSettings()
            val backup = BackupServer.Backup(
                expense = expenseDao.getAll(),
                currency = currencyDao.getAll(),
                expenses = expensesDao.getAllExpenses()
            )
            settings?.let {
                backupServer.getRestoreBackup(it.userName) { restoreBackup ->
                    Log.d("blablabla", "Date backup in server ${restoreBackup?.backupTime}")
                    val isSave = if (restoreBackup?.backupTime != null) {
                        val diff =
                            Calendar.getInstance().time.time - restoreBackup.backupTime.time

                        when (it.backupPeriod) {
                            1 -> (diff / (1000 * 60 * 60 * 12)) > 0
                            2 -> (diff / (1000 * 60 * 60 * 24)) > 0
                            3 -> (diff / (1000 * 60 * 60 * 24 * 7)) > 0
                            else -> false
                        }
                    } else true
                    if (isSave) {
                        Log.d("blablabla", "Make backup")
                        backupServer.saveBackupToServer(it.userName, backup)
                    }
                }
            }
        }
    }

    fun restoreBackupLiveData(idProgram: String) =
        backupServer.getRestoreBackupLiveData(idProgram)

    // Из ExpenseRepository
    fun getExpenseAllLiveData(): LiveData<List<Expense>> = expenseDao.getAllLiveData()
    fun getExpenseByName(expense: String) = expenseDao.search(expense)
    fun addExpense(expense: Expense) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expenseDao.addExpense(expense)
        }
    }

    suspend fun deleteExpenseRecord(expense: String, isDelete: Boolean): Long {
        var numberRecords = 0L
        if (isDelete) expenseDao.deleteExpenseByName(expense)
        else numberRecords = expensesDao.getExpensesCount(expense)
        return numberRecords

    }

    /*
    * Для изменения Expense
    */
    suspend fun updateExpense(newExpenseName: String, oldExpenseName: String) =
        expenseDao.updateRecord(newExpenseName, oldExpenseName)

    /*
    * Получение курсов валют за дату
    */
    private val exchangeRateMutableLiveDate = MutableLiveData<ResultCurrentExchangeRate>()
    val exchangeRateLiveDate: LiveData<ResultCurrentExchangeRate>
        get() = exchangeRateMutableLiveDate

    fun getExchangeRate(par: ResultCurrentExchangeRate) {
        exchangeRateMutableLiveDate.postValue(par)
    }

    private var findDate: Date = Calendar.getInstance().time
    fun setFindDate(date: Date) {
        findDate = date
    }

    fun getFoundDate() = findDate

    /*
    Search in Expenses
    */
    private val historySearchStringExpenses = mutableListOf<String>()
    fun getSearchExpenses(
        searchString: String,
        folder: String
    ): Flow<List<ExpensesExtendedView>> {
        if (searchString.isNotBlank() && !historySearchStringExpenses.contains(searchString)) historySearchStringExpenses.add(
            searchString
        )

        return currentSettingsFlow.flatMapLatest {
            expensesDao.getSearchExpenses(
                "%$searchString%",
                it.folder_id
            )
        }
    }

    fun getHistorySearchStringExpenses() = historySearchStringExpenses

    /*
    Work with folders
    */
    fun getAllFolders() = foldersDao.getAllExtendedViewFlow()

    //fun searchFolderById(folderId: String) = foldersDao.search(folderId)
    suspend fun addFolder(folder: Folders) = foldersDao.addFolder(folder)

    /*
    * Для изменения Folder
    */
    suspend fun deleteFolder(folderId: Long) {
        foldersDao.deleteFolderByName(folderId)
    }

    suspend fun updateFolder(folder: Folders) {
        foldersDao.update( folder )
    }
}