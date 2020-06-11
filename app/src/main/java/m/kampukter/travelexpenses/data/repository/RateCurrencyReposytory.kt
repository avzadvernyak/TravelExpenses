package m.kampukter.travelexpenses.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.RateCurrency
import m.kampukter.travelexpenses.data.dao.RateCurrencyDao
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.dto.RateCurrencyNbu
import retrofit2.Call
import retrofit2.Callback
import java.text.SimpleDateFormat
import java.util.*

class RateCurrencyRepository(
    private val rateCurrencyAPI: RateCurrencyAPI,
    private val rateCurrencyDao: RateCurrencyDao
) {
    fun getRateCurrencyNBU() {
        val call =
            rateCurrencyAPI.getRateCurrencyNbu("USD", "20200606", "json")
        call.enqueue(object : Callback<List<RateCurrencyNbu>> {
            override fun onResponse(
                call: Call<List<RateCurrencyNbu>>,
                response: retrofit2.Response<List<RateCurrencyNbu>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.first()?.let { rateCurrencyNBU ->
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(
                            rateCurrencyNBU.exchangedate
                        )?.let {
                            GlobalScope.launch(context = Dispatchers.IO) {
                                rateCurrencyDao.insert(
                                    RateCurrency(
                                        name = rateCurrencyNBU.cc,
                                        exchangeDate =it,
                                        rate = rateCurrencyNBU.rate
                                    )
                                )
                            }
                        }
                    }
                } else Log.d("blablabla", "isSuccessful is false")
            }

            override fun onFailure(call: Call<List<RateCurrencyNbu>>, t: Throwable) {
                t.message?.let {
                    Log.d("blablabla", "onFailure $it")
                }
            }
        })
    }
}