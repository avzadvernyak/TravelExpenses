package m.kampukter.travelexpenses.data

data class ExchangeCurrentRate (
    val currencyCode: String,
    val currencyName: String,
    val rate: Float,
    val exchangeDate: String
)