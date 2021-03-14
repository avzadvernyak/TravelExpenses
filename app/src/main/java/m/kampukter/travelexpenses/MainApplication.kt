package m.kampukter.travelexpenses

import android.app.Application
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.facebook.stetho.Stetho
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.*
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.FSRepository
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
import java.util.*

lateinit var mainApplication: MainApplication

const val DEFAULT_CURRENCY_CONST_RUB = 1
const val DEFAULT_CURRENCY_CONST_UAH = 2
const val DEFAULT_CURRENCY_CONST_BYN = 6

@Suppress("unused")
class MainApplication : Application() {

    private var currentAPIScope: Scope? = null

    private var currencySession: CurrencySession? = null

    private val module = module {
        single {
            Room.databaseBuilder(androidContext(), MyDatabase::class.java, "expenses.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(supportDb: SupportSQLiteDatabase) {
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().currencyDao().insertAll(
                                listOf(
                                    CurrencyTable(name = "RUB", defCurrency = 0),
                                    CurrencyTable(name = "UAH", defCurrency = 0),
                                    CurrencyTable(name = "USD", defCurrency = 0),
                                    CurrencyTable(name = "EUR", defCurrency = 1),
                                    CurrencyTable(name = "NOK", defCurrency = 0),
                                    CurrencyTable(name = "BYN", defCurrency = 0),
                                    CurrencyTable(name = "PLN", defCurrency = 0),
                                    CurrencyTable(name = "CZK", defCurrency = 0)
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
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().foldersDao().insertAll(
                                listOf(
                                    Folders( id = 1L ,shortName = "Расходы", description = "Папка расходов по умолчанию")
                                )
                            )
                        }
                        GlobalScope.launch(context = Dispatchers.IO) {
                            get<MyDatabase>().settingsDao().insert(
                                Settings(
                                    userName = "${Build.BRAND}-${Build.MODEL}-${UUID.randomUUID()}",
                                    defCurrency = 0,
                                    backupPeriod = 0,
                                    folder_id  = 1L
                                )
                            )
                        }
                    }
                }).build()
        }

        single<BackupServer> { FirebaseBackupServer() }
        single {
            ExpensesRepository(
                get<MyDatabase>().expensesDao(),
                get<MyDatabase>().rateCurrencyDao(),
                get<MyDatabase>().currencyDao(),
                get<MyDatabase>().settingsDao(),
                get<MyDatabase>().expenseDao(),
                get<MyDatabase>().foldersDao(),
                get()
            )
        }
        single<FileSystemAPI> { StandardFSAPI(this@MainApplication) }
        single {
            FSRepository( get(), get<MyDatabase>().expensesDao())
        }

        // Start Retrofit injection
        scope(named("API")) {
            scoped { retrofitBuild(getProperty("currentAPIUrl")) }
            scoped { get<Retrofit>().create(RateCurrencyAPI::class.java) }
            scoped {
                RateCurrencyAPIRepository(
                    get<MyDatabase>().expensesDao(),
                    get<MyDatabase>().rateCurrencyDao(),
                    get(),get()
                )
            }
        }
        // End Retrofit injection*/
        viewModel { MyViewModel(get(), get()) }
    }

    private fun retrofitBuild(apiUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(apiUrl)
            .addConverterFactory(
                when (getActiveCurrencySession()) {
                    DEFAULT_CURRENCY_CONST_RUB -> TikXmlConverterFactory.create()
                    else -> GsonConverterFactory.create()
                }
            ).build()
    }

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)
        mainApplication = this@MainApplication
        NetworkLiveData.init(this)

        startKoin {
            androidContext(this@MainApplication)
            modules(module)
        }
        // Delete Invalid Photo Files
       // getKoin().get<MyViewModel>().deleteInvalidFiles()

        GlobalScope.launch(context = Dispatchers.IO) {

            val mySettings = getKoin().get<ExpensesRepository>().getSettings()
            if (mySettings != null) {
                currencySession =
                    if (mySettings.defCurrency != 0) CurrencySession(mySettings.defCurrency)
                    else null
                when (mySettings.backupPeriod) {
                    1 -> getKoin().get<MyViewModel>().startBackup(Periodic.HalfDayBackup)
                    2 -> getKoin().get<MyViewModel>().startBackup(Periodic.DayBackup)
                    3 -> getKoin().get<MyViewModel>().startBackup(Periodic.WeekBackup)
                    else -> getKoin().get<MyViewModel>().stopBackup()
                }
            } /*else {
                getKoin().get<ExpensesRepository>().insertSettings(
                    Settings(
                        userName = "${Build.BRAND}-${Build.MODEL}-${UUID.randomUUID()}",
                        defCurrency = 0,
                        backupPeriod = 0,
                        folder_id  = 0L
                    )
                )
            }*/
        }
    }

    fun getActiveCurrencySession() = currencySession?.getCurrencyId()
    fun getCurrentScope() = currencySession?.getCurrentScope()
    fun changeActiveCurrency(currencyId: Int) {
        currencySession?.dispose()
        currencySession = if (currencyId != 0) CurrencySession(currencyId)
        else null
    }

    fun startAPISynch() {
        currencySession?.startSynch()
    }

    fun saveBackup() {
        getKoin().get<ExpensesRepository>().saveBackup()
    }
}
