package m.kampukter.travelexpenses.data

import androidx.room.Database
import androidx.room.RoomDatabase
import m.kampukter.travelexpenses.data.dao.CurrencyDao
import m.kampukter.travelexpenses.data.dao.ExpenseDao
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dao.TravelExpensesDao

@Database(
    version = 1, entities = [
        Expenses::class, Expense::class, Currency::class
    ]
)

abstract class MyDatabase : RoomDatabase() {

    abstract fun expensesDao(): ExpensesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun travelExpensesDao(): TravelExpensesDao

}