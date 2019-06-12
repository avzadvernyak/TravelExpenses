package m.kampukter.travelexpenses

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.repository.CurrencyRepository
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.TravelExpensesRepository
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication: Application() {
    private val module = module {

        single {
            Room.databaseBuilder(androidContext(), MyDatabase::class.java, "expenses.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(supportDb: SupportSQLiteDatabase) {
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().currencyDao().insertAll(
                                listOf(
                                    Currency(name = "UAH"),
                                    Currency(name = "RUR"),
                                    Currency(name = "USD"),
                                    Currency(name = "EUR")
                                )
                            )
                        }
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().expenseDao().insertAll(
                                listOf(
                                    Expense(name = "Питание - ощественное"),
                                    Expense(name = "Питание - продукты"),
                                    Expense(name = "Дорога - Топливо"),
                                    Expense(name = "Дорога - Оплата дорог"),
                                    Expense(name = "Дорога - Парковки"),
                                    Expense(name = "Дорога - Прочее"),
                                    Expense(name = "Проживание"),
                                    Expense(name = "Сувениры"),
                                    Expense(name = "Достопримечательности"),
                                    Expense(name = "Промтовары"),
                                    Expense(name = "Прочее")
                                )
                            )
                        }
                    }
                }).build()
        }

        single { CurrencyRepository(get<MyDatabase>().currencyDao()) }
        single { ExpenseRepository(get<MyDatabase>().expenseDao()) }
        single { ExpensesRepository(get<MyDatabase>().expensesDao()) }
        single { TravelExpensesRepository(get<MyDatabase>().travelExpensesDao()) }
        viewModel { MyViewModel(get(),get(),get(),get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }
}