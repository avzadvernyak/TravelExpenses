package m.kampukter.travelexpenses.data

sealed class ExpensesMainCollection( val id: Long, val contentHash: Int) {
    data class Header(val title: String) : ExpensesMainCollection(Long.MAX_VALUE , title.hashCode())
    data class Row(val expenses: ExpensesExtendedView) : ExpensesMainCollection( expenses.id,expenses.hashCode() )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExpensesMainCollection

        if (id != other.id) return false
        if (contentHash != other.contentHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + contentHash.hashCode()
        return result
    }
}
