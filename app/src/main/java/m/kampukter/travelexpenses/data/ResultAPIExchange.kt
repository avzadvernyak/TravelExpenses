package m.kampukter.travelexpenses.data

data class ResultAPIExchange<out T>(val status: Status, val data: T?, val errorMessage: String?) {

    enum class Status {
        SUCCESS,
        ERROR
    }

    companion object {
        fun <T> success(data: T?): ResultAPIExchange<T> {
            return ResultAPIExchange(Status.SUCCESS, data, null )
        }

        fun <T> error(message: String ): ResultAPIExchange<T> {
            return ResultAPIExchange(Status.ERROR, null, message)
        }
    }

    override fun toString(): String {
        return "Result(status=$status, data=$data,  message=$errorMessage)"
    }
}
