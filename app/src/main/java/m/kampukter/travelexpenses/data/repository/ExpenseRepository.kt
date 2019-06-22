package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.data.ExpenseDeletionResult
import m.kampukter.travelexpenses.data.dao.ExpenseDao
import m.kampukter.travelexpenses.data.dao.ExpensesDao

class ExpenseRepository(private val expenseDao: ExpenseDao , private val expensesDao: ExpensesDao) {
    fun getExpenseAll(): LiveData<List<Expense>> = expenseDao.getAll()
    fun getExpenseById(foundId: String) = expenseDao.search(foundId)
    fun addExpense(expense: Expense) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expenseDao.addExpense(expense)
        }
    }

    fun deleteExpenseById(selectedId: Long) {
        GlobalScope.launch(context = Dispatchers.IO) {
            expenseDao.deleteExpenseById(selectedId)
        }
    }
    /*
    * Для удаления Expense. Код Олега
    */
    fun deleteExpense(expenseId: Long, isForced: Boolean): LiveData<ExpenseDeletionResult> =
        liveData(context = Dispatchers.IO) {
            val countRecords = expensesDao.getExpensesCount(expenseId)
            if (isForced || countRecords == 0L) {
                expenseDao.deleteExpenseById(expenseId)
                emit(ExpenseDeletionResult.Success)
            } else {
                emit(ExpenseDeletionResult.Warning(countRecords))
            }
        }
}