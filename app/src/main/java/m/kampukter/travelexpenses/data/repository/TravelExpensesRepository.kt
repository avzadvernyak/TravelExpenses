package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.ExpensesSumView
import m.kampukter.travelexpenses.data.TravelExpensesView
import m.kampukter.travelexpenses.data.dao.TravelExpensesDao

class TravelExpensesRepository(private val travelExpensesDao: TravelExpensesDao) {
    fun getAll(): LiveData<List<TravelExpensesView>> = travelExpensesDao.getAll()
    fun getAllForSend(): LiveData<String> {
        var resultString = ""
        val result = MutableLiveData<String>()
        GlobalScope.launch(context = Dispatchers.IO) {
            travelExpensesDao.getAllExpenses().forEach {
                resultString = resultString + DateFormat.format(
                    "dd/MM/yyyy HH:mm",
                    it.dateTime
                ) + "," + it.expenseName + "," + it.sum + "," + it.currencyName + "," + it.note + "\n"
            }
            result.postValue(resultString)
        }
        return result
    }
    fun getRecordById(id: Long): LiveData<TravelExpensesView> = travelExpensesDao.getRecordById(id)
    fun getExpensesSum():  LiveData<List<ExpensesSumView>> = travelExpensesDao.getSumExpenses()
}