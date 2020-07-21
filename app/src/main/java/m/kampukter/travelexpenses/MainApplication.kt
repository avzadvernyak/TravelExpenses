package m.kampukter.travelexpenses

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.facebook.stetho.Stetho
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.Currency
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.data.MyDatabase
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.repository.ExpenseRepository
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.viewmodel.MyViewModel
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

lateinit var mainApplication: MainApplication

@Suppress("unused")
class MainApplication : Application() {

    private var currentAPIScope: Scope? = null

    private var currencySession: CurrencySession? = null

    private val module = module {
        single {
            Room.databaseBuilder(androidContext(), MyDatabase::class.java, "expenses.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(supportDb: SupportSQLiteDatabase) {
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().currencyDao().insertAll(
                                listOf(
                                    Currency(name = "RUB", defCurrency = 0),
                                    Currency(name = "UAH", defCurrency = 0),
                                    Currency(name = "USD", defCurrency = 0),
                                    Currency(name = "EUR", defCurrency = 1),
                                    Currency(name = "NOK", defCurrency = 0),
                                    Currency(name = "BYN", defCurrency = 0),
                                    Currency(name = "PLN", defCurrency = 0),
                                    Currency(name = "CZK", defCurrency = 0)
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
        single {
            ExpenseRepository(
                get<MyDatabase>().expenseDao(),
                get<MyDatabase>().expensesDao()
            )
        }
        single {
            ExpensesRepository(
                get<MyDatabase>().expensesDao(),
                get<MyDatabase>().rateCurrencyDao(),
                get<MyDatabase>().currencyDao(),
                get<MyDatabase>().settingsDao()
            )
        }

        // Start Retrofit injection
        scope(named("API")) {
            scoped { retrofitBuild(getProperty("currentAPIUrl")) }
            scoped { get<Retrofit>().create(RateCurrencyAPI::class.java) }
            scoped {
                RateCurrencyAPIRepository(
                    get<MyDatabase>().expensesDao(),
                    get<MyDatabase>().rateCurrencyDao(),
                    get()
                )
            }
        }
        // End Retrofit injection*/
        viewModel { MyViewModel(get(), get()) }
    }

    private fun retrofitBuild(apiUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(
                when (getActiveCurrencySession()) {
                    2 -> TikXmlConverterFactory.create()
                    else -> GsonConverterFactory.create()
                }
            ).build()


    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
        mainApplication = this@MainApplication
        NetworkLiveData.init(this)

        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }

        GlobalScope.launch(context = Dispatchers.IO) {
            val mySettings = getKoin().get<ExpensesRepository>().getSettings()
            mySettings?.let { currencySession = CurrencySession(it.defCurrency) }
        }
    }

    fun getActiveCurrencySession() = currencySession?.getCurrencyId()
    fun getCurrentScope() = currencySession?.getCurrentScope()
    fun changeActiveCurrency(currencyId: Int) {
        currencySession?.dispose()
        currencySession = CurrencySession(currencyId)
    }

    fun startAPISynch() {
        currencySession?.startSynch()
    }
}
