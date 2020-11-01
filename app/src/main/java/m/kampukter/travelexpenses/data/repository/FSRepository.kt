package m.kampukter.travelexpenses.data.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dto.FileSystemAPI
import java.io.File
import java.util.*

class FSRepository(
    private val fileSystemsAPI: FileSystemAPI,
    private val expensesDao: ExpensesDao
) {
    fun createJPGFile() = fileSystemsAPI.createFile("JPEG_", "jpg")
    fun deleteFile(file: File) {
        fileSystemsAPI.deleteFile(file)
    }

    suspend fun deleteInvalidFiles() {
        val listValidLinks = expensesDao.getAllExpenses()
            .mapNotNull { it.imageUri?.replace("%20", " ")?.substring(7) }
            .map { item -> File(item) }
        val listAllFile = fileSystemsAPI.getAllFilesDirectory()?.toMutableList()
        var n = 0
        listAllFile?.forEach { file ->
            if (!listValidLinks.contains(file)) {
                fileSystemsAPI.deleteFile(file)
                n += 1
            }
        }
        Log.d("blabla", "Deleted $n file(s)")
    }
}