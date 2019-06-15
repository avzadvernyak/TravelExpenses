package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.data.dao.ExpenseDao

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    fun getExpenseAll(): LiveData<List<Expense>> = expenseDao.getAll()
    fun getExpenseById( foundId: String) = expenseDao.search(foundId)
}