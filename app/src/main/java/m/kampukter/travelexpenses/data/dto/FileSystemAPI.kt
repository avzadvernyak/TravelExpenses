package m.kampukter.travelexpenses.data.dto

import java.io.File

interface FileSystemAPI {
    fun createFile( name: String, extension: String): File?
    fun deleteFile( file: File): Boolean
    fun getAllFilesDirectory(): Array<File>?
}