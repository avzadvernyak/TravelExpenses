package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import m.kampukter.travelexpenses.data.TravelExpensesView
import m.kampukter.travelexpenses.data.dao.TravelExpensesDao

class TravelExpensesRepository(private val travelExpensesDao: TravelExpensesDao) {
    fun getAll(): LiveData<List<TravelExpensesView>> = travelExpensesDao.getAll()
    fun getRecordById(id: Long): LiveData<TravelExpensesView> = travelExpensesDao.getRecordById(id)
}