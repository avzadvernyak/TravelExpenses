package m.kampukter.travelexpenses.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import m.kampukter.travelexpenses.data.dao.*

@Database(
    version = 1,exportSchema = false, entities = [
        Expenses::class, Expense::class, Currency::class, RateCurrency::class, Settings::class
    ]
)

//@TypeConverters(Converters::class)
@TypeConverters(DateConverter::class)

abstract class MyDatabase : RoomDatabase() {

    abstract fun expensesDao(): ExpensesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun rateCurrencyDao(): RateCurrencyDao
    abstract fun settingsDao(): SettingsDao
}