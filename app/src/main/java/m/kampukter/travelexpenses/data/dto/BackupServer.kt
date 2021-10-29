package m.kampukter.travelexpenses.data.dto

import androidx.lifecycle.LiveData
import m.kampukter.travelexpenses.data.CurrencyTable
import m.kampukter.travelexpenses.data.Expense
import m.kampukter.travelexpenses.data.ExpensesExtendedView
import java.util.*

interface BackupServer {

    fun saveBackupToServer(id: String, backup: Backup)
    fun getRestoreBackupLiveData(id: String): LiveData<Backup>?
    fun getRestoreBackup(id: String, onGetRestoreBackup: (Backup?) -> Unit)

    data class Backup(
        val backupTime: Date = Calendar.getInstance().time,
        val expense: List<Expense>,
        val currency: List<CurrencyTable>,
        val expenses: List<ExpensesExtendedView>
    )
}