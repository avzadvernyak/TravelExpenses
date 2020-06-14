package m.kampukter.travelexpenses.data.dto

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryName

interface RateCurrencyAPI {
    /*@GET("exchange?")
    suspend fun getRateCurrencyNbu(
        @Query("valcode") currency: String,
        @Query("date") date: String,
        @QueryName str: String
    ): Call<List<RateCurrencyNbu>>*/
    @GET("exchange?")
    suspend fun getRateCurrencyNbu(
        @Query("valcode") currency: String,
        @Query("date") date: String,
        @QueryName str: String
    ): Response<List<RateCurrencyNbu>>

}
data class RateCurrencyNbu(
    val r030 : Int,
    val txt: String,
    val rate: Float,
    val cc: String,
    val exchangedate: String
)