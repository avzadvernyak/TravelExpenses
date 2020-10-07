package m.kampukter.travelexpenses.data

import java.util.*

sealed class FilterForExpensesMap{
    object All : FilterForExpensesMap()
    data class ExpenseFilter (val expenseName: String): FilterForExpensesMap()
    data class DateFilter (val startPeriod: Date, val endPeriod: Date): FilterForExpensesMap()
    data class DateRangeFilter (val startPeriod: Long, val endPeriod: Long): FilterForExpensesMap()
}