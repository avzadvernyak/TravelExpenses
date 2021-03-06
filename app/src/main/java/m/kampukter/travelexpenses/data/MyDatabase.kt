package m.kampukter.travelexpenses.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import m.kampukter.travelexpenses.data.dao.*

@Database(
    version = 4, exportSchema = false, entities = [
        Expenses::class, Expense::class, CurrencyTable::class, RateCurrency::class,
        Settings::class, Folders::class
    ]
)

//@TypeConverters(Converters::class)
@TypeConverters(MyTypeConverter::class)

abstract class MyDatabase : RoomDatabase() {

    abstract fun expensesDao(): ExpensesDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun rateCurrencyDao(): RateCurrencyDao
    abstract fun settingsDao(): SettingsDao
    abstract fun foldersDao(): FoldersDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Expenses ADD COLUMN location TEXT default null"
        )
        database.execSQL(
            "ALTER TABLE Expenses ADD COLUMN imageUri TEXT default null"
        )
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val shortNameDefault = "Расходы"
        val descriptionDefault = "Папка по умолчанию"

        database.execSQL("CREATE TABLE folders (shortName TEXT PRIMARY KEY NOT NULL, description TEXT)")
        database.execSQL("INSERT OR REPLACE INTO folders (shortName,description) VALUES ('$shortNameDefault', '$descriptionDefault')")
        database.execSQL(
            "ALTER TABLE 'settings' ADD COLUMN 'folder' TEXT NOT NULL DEFAULT ('$shortNameDefault')"
        )
        database.execSQL(
            "CREATE TABLE UpdatedTableExpenses (id INTEGER PRIMARY KEY NOT NULL, dateTime TEXT NOT NULL, expense TEXT NOT NULL," +
                    " sum REAL NOT NULL, currency_field TEXT NOT NULL, note TEXT NOT NULL, location TEXT, imageUri TEXT, folder TEXT NOT NULL, " +
                    "FOREIGN KEY (expense) REFERENCES expense(name) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "FOREIGN KEY (currency_field) REFERENCES currency(name) ON UPDATE NO ACTION ON DELETE NO ACTION ," +
                    "FOREIGN KEY (folder) REFERENCES folders(shortName) ON UPDATE CASCADE ON DELETE CASCADE)"
        )
        database.execSQL(
            "ALTER TABLE 'expenses' ADD COLUMN 'folder' TEXT NOT NULL DEFAULT ('$shortNameDefault')"
        )
        database.execSQL(
            " INSERT OR REPLACE INTO UpdatedTableExpenses(id, dateTime, expense, sum, currency_field , note ," +
                    "location, imageUri, folder) " +
                    "SELECT id, dateTime, expense, sum, currency_field , note ,location, imageUri, folder FROM expenses "
        )
        database.execSQL("DROP TABLE expenses")
        database.execSQL("ALTER TABLE UpdatedTableExpenses RENAME TO expenses")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_currency_field ON expenses(currency_field)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_expense ON expenses(expense)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_folder ON expenses(folder)")
    }
}
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // Add FOREIGN KEY in settings table
        database.execSQL(
            "CREATE TABLE UpdatedTableSettings (userName TEXT PRIMARY KEY NOT NULL, defCurrency INTEGER NOT NULL, " +
                    " backupPeriod INTEGER NOT NULL, statusGPS INTEGER NOT NULL, folder TEXT NOT NULL, " +
                    "FOREIGN KEY (folder) REFERENCES folders(shortName) ON UPDATE CASCADE ON DELETE NO ACTION )"
        )
        database.execSQL(
            " INSERT OR REPLACE INTO UpdatedTableSettings(userName, defCurrency, backupPeriod, statusGPS, folder) " +
                    "SELECT userName, defCurrency, backupPeriod, statusGPS, folder FROM settings "
        )
        database.execSQL("DROP TABLE settings")
        database.execSQL("ALTER TABLE UpdatedTableSettings RENAME TO settings")
    }
}