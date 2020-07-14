package m.kampukter.travelexpenses

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class APISynchronizationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        Log.d("blablabla", "APISynchronizationWorker")
        mainApplication.startAPISynch()
        return Result.success()
    }
}