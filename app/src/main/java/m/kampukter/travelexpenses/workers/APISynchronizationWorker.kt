package m.kampukter.travelexpenses.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import m.kampukter.travelexpenses.mainApplication

class APISynchronizationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        mainApplication.startAPISynch()


        return Result.success()
    }
}