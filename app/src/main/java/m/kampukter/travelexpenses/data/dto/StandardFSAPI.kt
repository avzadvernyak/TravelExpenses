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
                getOutputDirectory(), name  + SimpleDateFormat(FILENAME, Locale.US)
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

    override fun getAllFilesDirectory(): Array<File>? = getOutputDirectory().listFiles()
   /* override fun deleteInvalidFiles( ){

        // Get root directory of media from navigation arguments
        val rootDirectory = File(getOutputDirectory().toString())


        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        val mediaList = rootDirectory.listFiles { file ->
            arrayOf("JPG").contains(file.extension.toUpperCase(Locale.ROOT))
        }
        Log.d("blabla"," ->$mediaList")

    }*/

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
