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
import m.kampukter.travelexpenses.data.RateCurrency
import m.kampukter.travelexpenses.data.ReportSumView
import m.kampukter.travelexpenses.data.dao.CurrencyDao
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dao.RateCurrencyDao
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.dto.RateCurrencyNbu
import retrofit2.Call
import retrofit2.Callback
import java.text.SimpleDateFormat
import java.util.*

class ExpensesRepository(
    private val expensesDao: ExpensesDao,
    private val rateCurrencyDao: RateCurrencyDao,
    private val currencyDao: CurrencyDao,
    private val rateCurrencyAPI: RateCurrencyAPI
) {

    fun getAll(): LiveData<List<Expenses>> = expensesDao.getAll()
    fun getRecordById(id: Long): LiveData<Expenses> = expensesDao.getExpensesById(id)

    suspend fun addExpenses(expenses: Expenses) {
        expensesDao.insert(expenses)
        getRateCurrencyNBU(
            expenses.currency,
            DateFormat.format("yyyyMMdd", expenses.dateTime).toString()
        )
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

    // API
    private suspend fun getRateCurrencyNBU(
        currencyFound: String,
        dateFound: String
    ): RateCurrencyNbu? {
        val rateCurrencyNBU =
            rateCurrencyAPI.getRateCurrencyNbu(currencyFound, dateFound, "json")
        Log.d("blablabla", "From API- $rateCurrencyNBU")
        return if (rateCurrencyNBU.isNullOrEmpty()) null
        else rateCurrencyNBU.first()
    }

    suspend fun rateSynchronizationNBU() {
        val currentDate = System.currentTimeMillis()
        val currencyList = currencyDao.getAll()
        currencyList.forEach { currency ->
            val resRate = rateCurrencyDao.searchByDate(currency.name, currentDate)
            //Log.d("blablabla", "Rate for ${currency.name} $resRate")
            if (resRate.isEmpty()) {
                Log.d("blablabla", "Go API find ${currency.name}")
                getRateCurrencyNBU(
                    currency.name,
                    DateFormat.format("yyyyMMdd", currentDate).toString()
                )?.let { rateCurrencyNBU ->
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(
                        rateCurrencyNBU.exchangedate
                    )?.let {
                        rateCurrencyDao.insert(
                            RateCurrency(
                                name = rateCurrencyNBU.cc,
                                exchangeDate = it,
                                rate = rateCurrencyNBU.rate
                            )
                        )
                    }
                }
            } else Log.d("blablabla", "Есть в базе ${currency.name}")
        }
    }
}