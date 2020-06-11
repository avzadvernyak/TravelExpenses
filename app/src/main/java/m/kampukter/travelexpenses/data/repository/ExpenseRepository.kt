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