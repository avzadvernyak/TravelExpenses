package m.kampukter.travelexpenses.data

sealed class ExpenseDeletionResult {
    object Success : ExpenseDeletionResult()
    data class Warning (val expenseId: Long, val countRecords: Long): ExpenseDeletionResult()
}