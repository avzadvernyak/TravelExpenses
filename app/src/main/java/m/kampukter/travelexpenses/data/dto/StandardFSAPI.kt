package m.kampukter.travelexpenses.data.dto

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import m.kampukter.travelexpenses.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class StandardFSAPI(
    private val context: Application,
) : FileSystemAPI {

    override fun createFile(name: String, extension: String): File? =
        try {
            val timeStamp = SimpleDateFormat(FILENAME, Locale.US)
                .format(System.currentTimeMillis())
            File(
                getOutputDirectory(), "$name$timeStamp.$extension"
            )
        } catch (exception: IOException) {
            Log.e("blabla", "Error (createTempFile): ${exception.message}")
            null
        }

    override fun deleteFile(file: File): Boolean =
        try {
            file.delete()
        } catch (exception: IOException) {
            Log.e("blabla", "Error (deleteFile): ${exception.message}")
            false
        }

    override fun deleteInvalidPhotoFiles(validFiles: List<Uri>) {
        val listAllFile = getAllFilesDirectory()
        var i = 0
        listAllFile?.forEach { file ->
            if (!validFiles.contains(file)) {
                deleteFile(file.toFile())
                i += 1
            }
        }
        Log.i("blabla", "Deleted $i file(s)")

    }

    override fun getAllFilesDirectory(): List<Uri>? {
        return getOutputDirectory().listFiles()?.map { file -> Uri.fromFile(file) }
    }

    private fun getOutputDirectory(): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }


    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

}
