package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.work.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.workers.BackupWorker
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dao.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.mainApplication
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

class ExpensesRepository(
    private val expensesDao: ExpensesDao,
    private val rateCurrencyDao: RateCurrencyDao,
    private val currencyDao: CurrencyDao,
    private val settingsDao: SettingsDao,
    private val expenseDao: ExpenseDao,
    private val backupServer: BackupServer
) {

    private var idWorkRequest: UUID? = null

    fun getAll(): LiveData<List<Expenses>> = expensesDao.getAll()
    fun getAllExpensesWithRate() = expensesDao.getAllExpensesWithRate()

    fun getRecordById(id: Long): LiveData<Expenses> = expensesDao.getExpensesById(id)

    suspend fun addExpenses(expenses: Expenses) {
        expensesDao.insert(expenses)
    }

    suspend fun updateExpenses(expenses: Expenses) {
        expensesDao.updateRecord(expenses)
    }

    suspend fun deleteExpensesById(selectedId: Long) {
        expensesDao.deleteExpensesById(selectedId)
    }

    suspend fun deleteAll() {
        expensesDao.deleteAll()
    }

    suspend fun getAllExpenses() = expensesDao.getAllExpenses()
    suspend fun getAllForSend(): LiveData<String> {
        var resultString = ""
        val result = MutableLiveData<String>()
        expensesDao.getAllExpenses().forEach {
            resultString = resultString + DateFormat.format(
                "dd/MM/yyyy HH:mm",
                it.dateTime
            ) + "," + it.expense + "," + it.sum + "," + it.currency + "," + it.note + "\n"
        }
        result.postValue(resultString)
        return result
    }

    fun getExpensesSum(): LiveData<List<ReportSumView>> = expensesDao.getSumExpenses()
    fun getCurrencySum(): LiveData<List<ReportSumView>> = expensesDao.getSumCurrency()

    // CurrencyDao
    fun getCurrencyAllLiveData(): LiveData<List<CurrencyTable>> = currencyDao.getAllLiveData()
    suspend fun getCurrencyAll(): List<CurrencyTable> = currencyDao.getAll()

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

    fun getAllRate() = rateCurrencyDao.getAll()

    // SettingsDao
    suspend fun getSettings() = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) = settingsDao.insert(settings)
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
                backupServer.getRestoreBackup(it.userName){ restoreBackup ->
                    Log.d("blablabla", "Date backup in server ${restoreBackup?.backupTime}")
                    var isSave = false
                    isSave = if (restoreBackup?.backupTime != null) {
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

    fun restoreBackupLiveData(idProgram: String) = backupServer.getRestoreBackupLiveData(idProgram)

    // Из ExpenseRepository
    fun getExpenseAllLiveData(): LiveData<List<Expense>> = expenseDao.getAllLiveData()
    suspend fun getExpenseAll(): List<Expense> = expenseDao.getAll()
    fun getExpenseByName(expense: String) = expenseDao.search(expense)
    fun addExpense(expense: Expense) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expenseDao.addExpense(expense)
        }
    }

    /*
    * Для удаления Expense. Код Олега
    */
    fun deleteExpense(expense: String, isForced: Boolean): LiveData<ExpenseDeletionResult> =
        liveData(context = Dispatchers.IO) {
            val countRecords = expensesDao.getExpensesCount(expense)
            if (isForced || countRecords == 0L) {
                expenseDao.deleteExpenseByName(expense)
                emit(ExpenseDeletionResult.Success)
            } else {
                emit(ExpenseDeletionResult.Warning(expense, countRecords))
            }
        }

}