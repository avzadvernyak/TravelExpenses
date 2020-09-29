package m.kampukter.travelexpenses.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import m.kampukter.travelexpenses.data.repository.RateCurrencyAPIRepository
import m.kampukter.travelexpenses.mainApplication
import org.koin.core.KoinComponent

class BredWorker(context: Context, params: WorkerParameters) :
    Worker(context, params), KoinComponent {

    override fun doWork(): Result {
        val rateRepository = mainApplication.getCurrentScope()?.get<RateCurrencyAPIRepository>()
        rateRepository?.let{ repository ->
            repository.bredInRepo()
        }
        return Result.success()
    }
}