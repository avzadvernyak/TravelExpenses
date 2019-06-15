package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Expenses
import m.kampukter.travelexpenses.data.dao.ExpensesDao

class ExpensesRepository(private val expensesDao: ExpensesDao) {
    fun getLastInputCurrent(): LiveData<Long> = expensesDao.getLastInputCurrent()
    fun addExpenses(expenses: Expenses) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.insert(expenses)
        }
    }

    fun deleteExpensesById(selectedId: Long) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expensesDao.deleteExpensesById(selectedId)
        }
    }
}