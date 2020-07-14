package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Currency
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.ReportSumView
import m.kampukter.travelexpenses.data.Settings
import m.kampukter.travelexpenses.data.dao.CurrencyDao
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dao.RateCurrencyDao
import m.kampukter.travelexpenses.data.dao.SettingsDao

class ExpensesRepository(
    private val expensesDao: ExpensesDao,
    private val rateCurrencyDao: RateCurrencyDao,
    private val currencyDao: CurrencyDao,
    private val settingsDao: SettingsDao
) {

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
    fun getCurrencyAllLiveData(): LiveData<List<Currency>> = currencyDao.getAllLiveData()
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

    /*fun getSettings() {
        GlobalScope.launch(context = Dispatchers.IO) {
            val mySettings = settingsDao.getSettings()
            Log.d("blablabla", "settings $mySettings")
        }
    }*/
    suspend fun getSettings() = settingsDao.getSettings()
    suspend fun insertSettings(settings: Settings) = settingsDao.insert(settings)

}