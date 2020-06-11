package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.ReportSumView
import m.kampukter.travelexpenses.data.dao.ExpensesDao

class ExpensesRepository(private val expensesDao: ExpensesDao) {

    fun getAll(): LiveData<List<Expenses>> = expensesDao.getAll()
    fun getRecordById(id: Long): LiveData<Expenses> = expensesDao.getExpensesById(id)

    fun addExpenses(expenses: Expenses) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.insert(expenses)
        }
    }

    fun updateExpenses(expenses: Expenses) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.updateRecord(expenses)
        }
    }

    fun deleteExpensesById(selectedId: Long) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.deleteExpensesById(selectedId)
        }
    }
    fun deleteAll() {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.deleteAll()
        }
    }
    fun getAllForSend(): LiveData<String> {
        var resultString = ""
        val result = MutableLiveData<String>()
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.getAllExpenses().forEach {
                resultString = resultString + DateFormat.format(
                    "dd/MM/yyyy HH:mm",
                    it.dateTime
                ) + "," + it.expense + "," + it.sum + "," + it.currency + "," + it.note + "\n"
            }
            result.postValue(resultString)
        }
        return result
    }

    fun getExpensesSum():  LiveData<List<ReportSumView>> = expensesDao.getSumExpenses()
    fun getCurrencySum():  LiveData<List<ReportSumView>> = expensesDao.getSumCurrency()
}