package m.kampukter.travelexpenses.data.dto

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import m.kampukter.travelexpenses.data.DateJsonConverter
import java.util.*

class FirebaseBackupServer : BackupServer {

    private var database: DatabaseReference? = null
    private val restore = MutableLiveData<BackupServer.Backup>()

    private val gson = GsonBuilder().disableHtmlEscaping().registerTypeAdapter(
        Date::class.java,
        DateJsonConverter()
    ).create()

    init {
        if (database == null) database = Firebase.database.reference
    }

    override fun saveBackupToServer(id: String, backup: BackupServer.Backup) {
        database?.child("travelexpenses")
            ?.child(id)
            ?.child("backup")
            ?.setValue(gson.toJson(backup))
    }

    override fun getRestoreBackupLiveData(id: String): LiveData<BackupServer.Backup>? {
        val listenerForSingleValueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val restoreBackup =
                    gson.fromJson(snapshot.value.toString(), BackupServer.Backup::class.java)
                restore.postValue(restoreBackup)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("blablabla", "snapshot error ${error.message} ")
            }
        }
        database?.child("travelexpenses")
            ?.child(id)
            ?.child("backup")
            ?.addListenerForSingleValueEvent(listenerForSingleValueEvent)
        return restore
    }
    override fun getRestoreBackup(id: String, onGetRestoreBackup: (BackupServer.Backup?) -> Unit) {
        val listenerForSingleValueEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val restoreBackup =
                    gson.fromJson(
                        snapshot.value.toString(),
                        BackupServer.Backup::class.java
                    )
                onGetRestoreBackup.invoke(restoreBackup)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("blablabla", "snapshot error ${error.message} ")
            }
        }
        database?.child("travelexpenses")
            ?.child(id)
            ?.child("backup")
            ?.addListenerForSingleValueEvent(listenerForSingleValueEvent)
    }
}
