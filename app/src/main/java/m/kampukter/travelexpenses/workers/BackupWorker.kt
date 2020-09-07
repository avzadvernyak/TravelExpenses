package m.kampukter.travelexpenses.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import m.kampukter.travelexpenses.mainApplication

class BackupWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        mainApplication.saveBackup()
        return Result.success()
    }
}