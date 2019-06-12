package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import m.kampukter.travelexpenses.data.Expenses

interface ExpensesDao: BasicDao<Expenses> {
    @Query("select * from expenses")
    fun getAll(): LiveData<List<Expenses>>
}