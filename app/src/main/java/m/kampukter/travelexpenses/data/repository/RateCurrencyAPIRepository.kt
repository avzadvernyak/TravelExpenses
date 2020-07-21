package m.kampukter.travelexpenses.data.repository

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import m.kampukter.travelexpenses.data.CurrentExchangeRate
import m.kampukter.travelexpenses.data.RateCurrency
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dao.RateCurrencyDao
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.dto.RateCurrencyNBRB
import m.kampukter.travelexpenses.data.dto.RateCurrencyNbu
import m.kampukter.travelexpenses.data.dto.ValCurs
import m.kampukter.travelexpenses.mainApplication
import org.koin.core.KoinComponent
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RateCurrencyAPIRepository(
    private val expensesDao: ExpensesDao,
    private val rateCurrencyDao: RateCurrencyDao,
    private val rateCurrencyAPI: RateCurrencyAPI
) : KoinComponent {
    private val defaultProgramCurrency = mainApplication.getActiveCurrencySession()

    suspend fun getCurrentRate(): LiveData<List<CurrentExchangeRate>> {
        return when (defaultProgramCurrency) {
            1 -> getRateNBU()
            2 -> getRateCBR()
            3 -> getRateNBRB()
            else -> MutableLiveData<List<CurrentExchangeRate>>()
        }
    }

    private suspend fun getRateNBU(): LiveData<List<CurrentExchangeRate>> {
        val resultListRate = mutableListOf<CurrentExchangeRate>()
        val retValue = MutableLiveData<List<CurrentExchangeRate>>()
        try {
            val response = rateCurrencyAPI.getRateTodayNbu("json")
            if (response.code() == 200) {
                response.body()?.forEach {
                    resultListRate.add(
                        CurrentExchangeRate(
                            currencyCode = it.cc,
                            currencyName = it.txt,
                            rate = it.rate,
                            exchangeDate = it.exchangedate
                        )
                    )
                }
            }
            retValue.postValue(resultListRate)
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyNbu) $e")
        }

        return retValue
    }

    private suspend fun getRateCBR(): LiveData<List<CurrentExchangeRate>> {
        val resultListRate = mutableListOf<CurrentExchangeRate>()
        val retValue = MutableLiveData<List<CurrentExchangeRate>>()
        try {
            val response = rateCurrencyAPI.getRateTodayCBR()
            if (response.code() == 200) {
                val date = DateFormat.format("dd.MM.yyyy", response.body()?.date).toString()
                response.body()?.valute?.forEach { _valute ->
                    resultListRate.add(
                        CurrentExchangeRate(
                            currencyCode = _valute.charCode,
                            currencyName = _valute.name,
                            rate = _valute.value / _valute.nominal,
                            exchangeDate = date
                        )
                    )
                }
            }
            retValue.postValue(resultListRate)
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyNbu) $e")
        }
        return retValue
    }

    private suspend fun getRateNBRB(): LiveData<List<CurrentExchangeRate>> {
        val resultListRate = mutableListOf<CurrentExchangeRate>()
        val retValue = MutableLiveData<List<CurrentExchangeRate>>()
        try {
            val response = rateCurrencyAPI.getRateTodayNBRB("0")
            if (response.code() == 200) {

                val date = response.body()?.first()?.Date?.let { _date ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                        _date
                    )
                }

                val dateString = DateFormat.format("dd.MM.yyyy", date).toString()

                response.body()?.forEach {
                    resultListRate.add(
                        CurrentExchangeRate(
                            currencyCode = it.Cur_Abbreviation,
                            currencyName = it.Cur_Name,
                            rate = it.Cur_OfficialRate / it.Cur_Scale,
                            exchangeDate = dateString
                        )
                    )
                }
            }
            retValue.postValue(resultListRate)
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyNbu) $e")
        }
        return retValue


    }

    suspend fun rateSynchronization() {

        when (defaultProgramCurrency) {
            1 -> {
                Log.d("Worker", "TravelExpenses -> SynchronizationNBU")
                rateSynchronizationNBU()
            }
            2 -> {
                Log.d("Worker", "TravelExpenses -> SynchronizationCBR")
                rateSynchronizationCBR()
            }
            3 -> {
                Log.d("Worker", "TravelExpenses -> SynchronizationNBRB")
                rateSynchronizationNBRB()
            }
        }
    }

    // Работа с API НБУ
    private suspend fun getRateCurrencyNBU(
        currencyFound: String,
        dateFound: String
    ): RateCurrencyNbu? {
        var response: Response<List<RateCurrencyNbu>>? = null
        try {
            response = rateCurrencyAPI.getRateCurrencyNbu(currencyFound, dateFound, "json")
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyNbu) $e")
        }

        //Log.d("blablabla", "Code HTTP- ${response?.code()}")
        if (response?.code() != 200) return null

        val rateCurrencyNBU = response.body()

        return if (rateCurrencyNBU.isNullOrEmpty()) null
        else rateCurrencyNBU.first()
    }

    // Работа с API НБУ
    private suspend fun rateSynchronizationNBU() {
        val infoRateList = expensesDao.getInfoForRate()
        //Log.d("blablabla", "infoForRate  NBU $infoRateList")
        infoRateList.forEach { infoRateItem ->
            val currencySearchResult =
                rateCurrencyDao.searchByDate(infoRateItem.currency_field, infoRateItem.dateRate)
            if (currencySearchResult == 0L) {
                val dateExchange = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(infoRateItem.dateRate)
                getRateCurrencyNBU(
                    infoRateItem.currency_field,
                    DateFormat.format("yyyyMMdd", dateExchange).toString()
                )?.let { rateCurrencyNBU ->
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(
                        rateCurrencyNBU.exchangedate
                    )?.let {
                        rateCurrencyDao.insert(
                            RateCurrency(
                                name = rateCurrencyNBU.cc,
                                exchangeDate = it,
                                rate = rateCurrencyNBU.rate
                            )
                        )
                    }
                }
            }
        }
    }

    // Работа с API ЦБР
    private suspend fun getRateCurrencyCBR(dateFound: String): ValCurs? {
        try {
            val response = rateCurrencyAPI.getRateCurrencyCBR(dateFound)
            if (response.code() != 200) return null
            return response.body()
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyCBR) $e ")
            return null
        }

    }

    // Работа с API ЦБР
    private suspend fun rateSynchronizationCBR() {
        val infoRateList = expensesDao.getInfoForRate()
        //Log.d("blablabla", "infoForRates  CBR $infoRateList")
        infoRateList.forEach { infoRateItem ->
            if (infoRateItem.currency_field != "RUB") {

                val currencySearchResult =
                    rateCurrencyDao.searchByDate(infoRateItem.currency_field, infoRateItem.dateRate)
                if (currencySearchResult == 0L) {
                    val listCurrenciesUsed =
                        infoRateList.filter { it.dateRate == infoRateItem.dateRate }
                            .map { it.currency_field }

                    val dateExchange = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(infoRateItem.dateRate)

                    if (dateExchange != null) {
                        val response = getRateCurrencyCBR(
                            DateFormat.format("dd.MM.yyyy", dateExchange).toString()
                        )
                            ?: return
                        response.valute.forEach { _valute ->
                            if (listCurrenciesUsed.contains(_valute.charCode)) {
                                rateCurrencyDao.insert(
                                    RateCurrency(
                                        name = _valute.charCode,
                                        exchangeDate = dateExchange,
                                        rate = _valute.value / _valute.nominal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Работа с API НБРБ
    private suspend fun getRateCurrencyNBRB(
        currencyFound: String,
        dateFound: String
    ): RateCurrencyNBRB? {
        try {
            val response = rateCurrencyAPI.getRateCurrencyNBRB(currencyFound, dateFound, "2")
            return if (response.code() != 200) return null
            else response.body()
        } catch (e: IOException) {
            Log.e("blablabla", " Error in API (getRateCurrencyNBRB) $e")
            return null
        }

    }

    // Работа с API НБPB
    private suspend fun rateSynchronizationNBRB() {
        val infoRateList = expensesDao.getInfoForRate()
        //Log.d("blablabla", "infoForRate  NBRB $infoRateList")
        infoRateList.forEach { infoRateItem ->
            val currencySearchResult =
                rateCurrencyDao.searchByDate(infoRateItem.currency_field, infoRateItem.dateRate)
            if (currencySearchResult == 0L) {
                getRateCurrencyNBRB(
                    infoRateItem.currency_field,
                    infoRateItem.dateRate
                )?.let { rateCurrencyNBRB ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                        rateCurrencyNBRB.Date
                    )?.let {
                        rateCurrencyDao.insert(
                            RateCurrency(
                                name = rateCurrencyNBRB.Cur_Abbreviation,
                                exchangeDate = it,
                                rate = rateCurrencyNBRB.Cur_OfficialRate / rateCurrencyNBRB.Cur_Scale
                            )
                        )
                    }
                }
            }
        }
    }

}