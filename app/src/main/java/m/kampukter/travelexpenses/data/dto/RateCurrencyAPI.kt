package m.kampukter.travelexpenses.data.dto

import com.google.gson.annotations.SerializedName
import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml
import m.kampukter.travelexpenses.data.CBRDateConverter
import m.kampukter.travelexpenses.data.CBRFloatConverter
import m.kampukter.travelexpenses.data.CBRIntConverter
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface RateCurrencyAPI {
    @GET("exchange?")
    suspend fun getRateCurrencyNbu(
        @Query("valcode") currency: String,
        @Query("date") date: String,
        @QueryName str: String
    ): Response<List<RateCurrencyNbu>>

    @GET("exchange?")
    suspend fun getRateTodayNbu(
        @Query("date") date: String,
        @QueryName str: String
    ): Response<List<RateCurrencyNbu>>

    @GET("rates/{currency}")
    suspend fun getRateCurrencyNBRB(
        @Path("currency") currencyString: String,
        @Query("ondate") date: String,
        @Query("parammode") str: String
    ): Response<RateCurrencyNBRB>

    @GET("rates")
    suspend fun getRateTodayNBRB(
        @Query("ondate") date: String,
        @Query("periodicity") str: String
    ): Response<List<RateCurrencyNBRB>>

    @GET("XML_daily.asp")
    suspend fun getRateCurrencyCBR(
        @Query("date_req") date: String
    ): Response<ValCurs>

    @Headers("Accept-Charset: utf-8")
    @GET("XML_daily_eng.asp")
    suspend fun getRateTodayCBR(
        @Query("date_req") date: String
    ): Response<ValCurs>



}

data class RateCurrencyNbu(
    val r030: Int,
    val txt: String,
    val rate: Float,
    val cc: String,
    val exchangedate: String
)


data class RateCurrencyNBRB(
    @SerializedName("Cur_ID")
    val curID: Int,
    @SerializedName("Date")
    val Date: String,
    @SerializedName("Cur_Abbreviation")
    val Cur_Abbreviation: String,
    @SerializedName("Cur_Scale")
    val Cur_Scale: Int,
    @SerializedName("Cur_Name")
    val Cur_Name: String,
    @SerializedName("Cur_OfficialRate")
    val Cur_OfficialRate: Float
)

@Xml
data class ValCurs(
    @Attribute(name = "name")
    var name: String? = null,

    @Attribute(name = "Date", converter = CBRDateConverter::class)
    var date: Date? = null,

    @Element(name = "Valute")
    var valute: List<Valute>
)

@Xml
data class Valute(
    @Attribute(name = "ID")
    var id: String = "",

    @PropertyElement(name = "NumCode")
    var numCode: String = "",

    @PropertyElement(name = "CharCode")
    var charCode: String = "",

    @PropertyElement(name = "Nominal", converter = CBRIntConverter::class)
    var nominal: Int = 0,

    @PropertyElement(name = "Name")
    var name: String = "",

    @PropertyElement(name = "Value", converter = CBRFloatConverter::class)
    var value: Float = 0F
)
