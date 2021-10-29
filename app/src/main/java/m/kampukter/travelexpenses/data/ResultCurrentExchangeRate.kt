package m.kampukter.travelexpenses.data

sealed class ResultCurrentExchangeRate {
    object Loading: ResultCurrentExchangeRate()
    data class Success(val exchangeCurrentRate: List<ExchangeCurrentRate>) : ResultCurrentExchangeRate()
    data class ErrorAPI( val tError: String ) : ResultCurrentExchangeRate()
}