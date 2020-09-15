package m.kampukter.travelexpenses

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.facebook.stetho.Stetho
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.*
import m.kampukter.travelexpenses.data.dto.BackupServer
import m.kampukter.travelexpenses.data.dto.FirebaseBackupServer
import m.kampukter.travelexpenses.data.dto.RateCurrencyAPI
import m.kampukter.travelexpenses.data.repository.ExpensesRepository
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.ui.SettingsActivity
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
import kotlin.math.absoluteValue

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
                get()
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
        viewModel { MyViewModel(get()) }
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
            if (mySettings != null) {
                val myHash = mySettings.userName.hashCode().absoluteValue
                val  testString = "${mySettings.userName}-${myHash.toString(16)}"
                val res = testString.split("-").last()
                Log.d("blablabla", "hashCode $res")
                currencySession =
                    if (mySettings.defCurrency != 0) CurrencySession(mySettings.defCurrency)
                    else null
                when (mySettings.backupPeriod) {
                    1 -> getKoin().get<MyViewModel>().startBackup(Periodic.HalfDayBackup)
                    2 -> getKoin().get<MyViewModel>().startBackup(Periodic.DayBackup)
                    3 -> getKoin().get<MyViewModel>().startBackup(Periodic.WeekBackup)
                    else -> getKoin().get<MyViewModel>().stopBackup()
                }
            } else {
                getKoin().get<ExpensesRepository>().insertSettings(
                    Settings(
                        userName = "${Build.BRAND}-${Build.MODEL}-${UUID.randomUUID()}",
                        defCurrency = 0,
                        backupPeriod = 0
                    )
                )
                startActivity(
                    Intent(baseContext, SettingsActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                )
            }
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
