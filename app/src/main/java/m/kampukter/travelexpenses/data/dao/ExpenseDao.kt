package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import m.kampukter.travelexpenses.data.Expense

interface ExpenseDao: BasicDao<Expense> {
    @Query("select * from expense")
    fun getAll(): LiveData<List<Expense>>
}