package m.kampukter.travelexpenses.data

sealed class ExpenseDeletionResult {
    object Success : ExpenseDeletionResult()
    data class Warning (val expenseName: String, val countRecords: Long): ExpenseDeletionResult()
}