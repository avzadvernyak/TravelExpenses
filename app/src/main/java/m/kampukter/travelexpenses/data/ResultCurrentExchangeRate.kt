package m.kampukter.travelexpenses.data

sealed class ResultCurrentExchangeRate {
    data class Success(val currentExchangeRate: List<CurrentExchangeRate>) : ResultCurrentExchangeRate()
    data class ErrorAPI( val tError: String ) : ResultCurrentExchangeRate()
}