package m.kampukter.travelexpenses

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class APISynchronizationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        mainApplication.startAPISynch()


        return Result.success()
    }
}