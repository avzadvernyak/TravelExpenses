package m.kampukter.travelexpenses.data.repository

import android.net.Uri
import androidx.core.net.toFile
import m.kampukter.travelexpenses.data.dao.ExpensesDao
import m.kampukter.travelexpenses.data.dto.FileSystemAPI
import java.io.File

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
            .mapNotNull { it.imageUri }.map { item -> Uri.parse(item) }
        fileSystemsAPI.deleteInvalidPhotoFiles(listValidLinks)
    }

    fun getMediaFiles(): List<File>? = fileSystemsAPI.getAllFilesDirectory()?.map { file -> file.toFile() }
}