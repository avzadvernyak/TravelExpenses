package m.kampukter.travelexpenses.data.repository

import android.app.Application
import m.kampukter.travelexpenses.data.dto.FileSystemAPI
import java.io.File

class FSRepository(
    private val fileSystemsAPI: FileSystemAPI
) {
    fun createJPGFile() = fileSystemsAPI.createFile("JPEG_", "jpg")
    fun deleteFile(file: File) = fileSystemsAPI.deleteFile(file)
}