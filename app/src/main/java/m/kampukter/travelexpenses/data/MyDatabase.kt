package m.kampukter.travelexpenses.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import m.kampukter.travelexpenses.data.dao.*

@Database(
    version = 6, exportSchema = false, entities = [
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
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Updated Folders
        database.execSQL("CREATE TABLE UpdatedTableFolders (id INTEGER NOT NULL PRIMARY KEY, shortName TEXT NOT NULL, description TEXT)")
        database.execSQL("INSERT OR REPLACE INTO UpdatedTableFolders( shortName, description) SELECT shortName, description FROM folders")
        database.execSQL("DROP TABLE folders")
        database.execSQL("ALTER TABLE UpdatedTableFolders RENAME TO folders")

        // Updated Expenses
        database.execSQL(
            "CREATE TABLE UpdatedTableExpenses (id INTEGER PRIMARY KEY NOT NULL, dateTime TEXT NOT NULL, expense TEXT NOT NULL," +
                    " sum REAL NOT NULL, currency_field TEXT NOT NULL, note TEXT NOT NULL, location TEXT, imageUri TEXT, folder_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (expense) REFERENCES expense(name) ON UPDATE CASCADE ON DELETE CASCADE," +
                    "FOREIGN KEY (currency_field) REFERENCES currency(name) ON UPDATE NO ACTION ON DELETE NO ACTION ," +
                    "FOREIGN KEY (folder_id) REFERENCES folders(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        database.execSQL(
            " INSERT OR REPLACE INTO UpdatedTableExpenses(id, dateTime, expense, sum, currency_field , note ," +
                    "location, imageUri, folder_id) " +
                    "SELECT id, dateTime, expense, sum, currency_field , note ,location, imageUri," +
                    "(select id from folders where folders.shortName = expenses.folder) as folder_id  FROM expenses "
        )
        database.execSQL("DROP TABLE expenses")
        database.execSQL("ALTER TABLE UpdatedTableExpenses RENAME TO expenses")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_currency_field ON expenses(currency_field)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_expense ON expenses(expense)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_folder ON expenses(folder_id)")

        // Updated Settings
        database.execSQL(
            "CREATE TABLE UpdatedTableSettings (userName TEXT PRIMARY KEY NOT NULL, defCurrency INTEGER NOT NULL, " +
                    " backupPeriod INTEGER NOT NULL, statusGPS INTEGER NOT NULL, folder_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (folder_id) REFERENCES folders(id) ON UPDATE NO ACTION ON DELETE NO ACTION )"
        )
        database.execSQL(
            " INSERT OR REPLACE INTO UpdatedTableSettings(userName, defCurrency, backupPeriod, statusGPS, folder_id) " +
                    "SELECT userName, defCurrency, backupPeriod, statusGPS,"+
                    "(select id from folders where folders.shortName = settings.folder) as folder_id FROM settings "
        )
        database.execSQL("DROP TABLE settings")
        database.execSQL("ALTER TABLE UpdatedTableSettings RENAME TO settings")


    }
}
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Updated Expense
        database.execSQL("CREATE TABLE UpdatedTableExpense (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL)")
        database.execSQL("INSERT OR REPLACE INTO UpdatedTableExpense( name ) SELECT name FROM expense")
        database.execSQL("DROP TABLE expense")
        database.execSQL("ALTER TABLE UpdatedTableExpense RENAME TO expense")

        // Updated Expenses
        database.execSQL(
            """CREATE TABLE UpdatedTableExpenses (id INTEGER PRIMARY KEY NOT NULL, dateTime TEXT NOT NULL, expense_id INTEGER NOT NULL,
                    sum REAL NOT NULL, currency_field TEXT NOT NULL, note TEXT NOT NULL default "1", location TEXT, imageUri TEXT, folder_id INTEGER NOT NULL, 
                    FOREIGN KEY (expense_id) REFERENCES expense(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY (currency_field) REFERENCES currency(name) ON UPDATE NO ACTION ON DELETE NO ACTION ,
                    FOREIGN KEY (folder_id) REFERENCES folders(id) ON UPDATE NO ACTION ON DELETE CASCADE)"""
        )
        database.execSQL(
            """ INSERT OR REPLACE INTO UpdatedTableExpenses(id, dateTime, sum, currency_field , note ,
                    location, imageUri, folder_id,  expense_id ) 
                    SELECT expenses.id as id, dateTime, sum, currency_field , expenses.note as note ,location, imageUri, folder_id,
                    (select A.id from expense A where A.name = expenses.expense) as expense_id  FROM expenses """
        )
        database.execSQL("DROP TABLE expenses")
        database.execSQL("ALTER TABLE UpdatedTableExpenses RENAME TO expenses")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_currency_field ON expenses(currency_field)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_expense ON expenses(expense_id)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_folder ON expenses(folder_id)")


    }
}