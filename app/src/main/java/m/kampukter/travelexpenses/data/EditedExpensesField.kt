package m.kampukter.travelexpenses.data

sealed class EditedExpensesField{
    data class ImageUriField( val idExpenses: Long, val uri: String?) : EditedExpensesField()
    data class SumField( val idExpenses: Long, val sum: Double) : EditedExpensesField()
    data class NoteField( val idExpenses: Long, val note: String) : EditedExpensesField()
    data class ExpenseField( val idExpenses: Long, val expense: Expense) : EditedExpensesField()
    data class CurrencyField( val idExpenses: Long, val currency: CurrencyTable) : EditedExpensesField()
}
