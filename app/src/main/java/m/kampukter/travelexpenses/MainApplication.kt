package m.kampukter.travelexpenses

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainApplication: Application() {
    private val module = module {

        single {
            Room.databaseBuilder(androidContext(), MyDatabase::class.java, "expenses.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(supportDb: SupportSQLiteDatabase) {
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().currencyDao().insertAll(
                                listOf(
                                    Currency(name = "UAH",defCurrency =0),
                                    Currency(name = "RUB",defCurrency =0),
                                    Currency(name = "USD",defCurrency =0),
                                    Currency(name = "EUR",defCurrency =1)
                                )
                            )
                        }
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().expenseDao().insertAll(
                                listOf(
                                    Expense(name = "Питание - общепит"),
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

        single { ExpenseRepository(get<MyDatabase>().expenseDao(), get<MyDatabase>().expensesDao()) }
        single {
            ExpensesRepository(
                get<MyDatabase>().expensesDao(),
                get<MyDatabase>().rateCurrencyDao(),
                get<MyDatabase>().currencyDao(),
                get()

            ) }

        // Start Retrofit injection
        single { Retrofit.Builder()
            .baseUrl("https://bank.gov.ua/NBUStatService/v1/statdirectory/")
            .addConverterFactory(GsonConverterFactory.create())
            .build() }
        single { get<Retrofit>().create(RateCurrencyAPI::class.java) }
        // End Retrofit injection

        viewModel { MyViewModel(get(),get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
    }
}