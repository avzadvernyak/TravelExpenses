package m.kampukter.travelexpenses

import androidx.work.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.workers.APISynchronizationWorker
import org.koin.core.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import java.util.*
import java.util.concurrent.TimeUnit


/*
- типовое решение было бы сделать компонент который представлял бы собой состояние твоего скоупа
- какой-нибудь currencysession в твоем случае
- потому как у тебя скоупы по типу используемой валюты переключаются если верно помню
- соответственно, в это состояние будет входить скоуп для этой валюты дерева зависимостей, коиновый
 скоуп для корутин, потому что есть четко очерченный жизненный цикл и служебная информация типо чо это за валюта
- инжектить это естественно не надо - по идее у тебя в аппликейшене должен быть какой-то
вызов по типу getactivecurrencysession? который бы возвращал текущую актуальную сессию
- жизненный цикл этой сессии соответственно управляется из апликейшна

попалась ссылка - https://devcolibri.com/kotlin-coroutines-patterns-anti-patterns/

 */

class CurrencySession(private val currencyId: Int) : KoinComponent {

    private var workerId: UUID? = null
    private val scope = MainScope()
    private val currentAPIScope: Scope = getKoinScope(currencyId).also {
        workerId = startPeriodicSynchronization()
    }
    fun startSynch() {
        scope.launch { currentAPIScope.get<RateCurrencyAPIRepository>().rateSynchronization(currencyId) }
    }
    fun dispose() {
        workerId?.let { WorkManager.getInstance(mainApplication).cancelWorkById(it) }
        currentAPIScope.close()

    }
    fun getCurrentScope() = currentAPIScope
    fun getCurrencyId() = currencyId
    private fun getKoinScope(id: Int): Scope {
        with(getKoin()) {
            when (id) {
                // Гривна по умолчанию
                DEFAULT_CURRENCY_CONST_UAH -> setProperty(
                    "currentAPIUrl",
                    "https://bank.gov.ua/NBUStatService/v1/statdirectory/"
                )
                // Рубль по умолчению
                DEFAULT_CURRENCY_CONST_RUB -> setProperty("currentAPIUrl", "http://www.cbr.ru/scripts/")
                // Белорусский рубль по умолчению
                DEFAULT_CURRENCY_CONST_BYN -> setProperty("currentAPIUrl", "https://www.nbrb.by/api/exrates/")
                // для отладки
                else -> setProperty("currentAPIUrl", "http://www.orbis.in.ua/")
            }
            return createScope("apiScope", named("API"))
        }
    }

    private fun startPeriodicSynchronization(): UUID {
        val myWorkRequest = PeriodicWorkRequest.Builder(
            APISynchronizationWorker::class.java,
            3,
            TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager
            .getInstance(mainApplication)
            .enqueueUniquePeriodicWork(
                "API_Synchronization",
                ExistingPeriodicWorkPolicy.REPLACE,
                myWorkRequest
            )
        return myWorkRequest.id
    }


}