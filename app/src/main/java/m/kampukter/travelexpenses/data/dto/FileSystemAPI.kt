package m.kampukter.travelexpenses.data.dto

import android.net.Uri
import java.io.File

interface FileSystemAPI {
    fun createFile( name: String, extension: String): File?
    fun deleteFile( file: File): Boolean
    fun getAllFilesDirectory(): List<Uri>?
    fun deleteInvalidPhotoFiles(validFiles: List<Uri>)
}