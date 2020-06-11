package m.kampukter.travelexpenses.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Currency
import m.kampukter.travelexpenses.data.dao.CurrencyDao

class CurrencyRepository(private val currencyDao: CurrencyDao) {
    fun getCurrencyAll(): LiveData<List<Currency>> = currencyDao.getAll()
    fun setDefCurrency(currencyName: String ) {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.setDefault(currencyName)
        }
    }
    fun resetDef(  ) {
        GlobalScope.launch(context = Dispatchers.IO) {
            currencyDao.resetDef()
        }
    }
}