package m.kampukter.travelexpenses.data

sealed class FilterForExpensesMap{
    object All : FilterForExpensesMap()
    data class ExpenseFilter (val expense: Expense): FilterForExpensesMap()
    data class DateRangeFilter (val startPeriod: Long, val endPeriod: Long): FilterForExpensesMap()
}