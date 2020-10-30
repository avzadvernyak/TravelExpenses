package m.kampukter.travelexpenses.data.dto

import android.app.Application
import android.util.Log
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
            File(
                getOutputDirectory(), name + "_" + SimpleDateFormat(FILENAME, Locale.US)
                    .format(System.currentTimeMillis()) + "." + extension
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
/*
val EXTENSION_WHITELIST = arrayOf("JPG")
            val mediaList = getOutputDirectory().listFiles { file ->
                EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
            }?.sortedDescending()?.toMutableList() ?: mutableListOf()
            Log.d("blabla", "mediaList: $mediaList")
 */
}
