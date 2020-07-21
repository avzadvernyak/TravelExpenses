package m.kampukter.travelexpenses.data

data class CurrentExchangeRate (
    val currencyCode: String,
    val currencyName: String,
    val rate: Float,
    val exchangeDate: String
)