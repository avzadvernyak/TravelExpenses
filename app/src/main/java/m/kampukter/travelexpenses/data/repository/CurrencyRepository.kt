package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Currency
import m.kampukter.travelexpenses.data.dao.CurrencyDao

class CurrencyRepository(private val currencyDao: CurrencyDao) {
    fun getCurrencyAll(): LiveData<List<Currency>> = currencyDao.getAll()
    fun getDefCurrency(): LiveData<Currency> = currencyDao.searchDefault()
    fun searchByName(query: String): LiveData<Currency> = currencyDao.searchByName(query)
    fun setDefCurrency( currencyId: Long ) {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.setDefault(currencyId)
        }
    }
    fun resetDef(  ) {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.resetDef()
        }
    }
}