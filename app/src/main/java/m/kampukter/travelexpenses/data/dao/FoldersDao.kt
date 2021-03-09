package m.kampukter.travelexpenses.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import m.kampukter.travelexpenses.data.Folders
import m.kampukter.travelexpenses.data.FoldersExtendedView

@Dao
interface FoldersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expensesProfiles: List<Folders>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFolder( folders: Folders)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add( expensesProfiles: Folders)

    @Query("select * from folders")
    fun getAllLiveData(): LiveData<List<Folders>>

    @Query("select * from folders where id = :idFolder")
    fun searchById( idFolder: Long): Folders
/*
    @Query("select * from folders where shortName = :query limit 1")
    fun search(query: String): LiveData<FoldersExtendedView>

     @Query("delete from folders WHERE folders.shortName = :selectedFolder")
     suspend fun deleteFolderByName(selectedFolder: String)

     @Query("update folders set shortName = :newFolderName where shortName = :oldFolderName ")
     suspend fun updateShortName(newFolderName: String, oldFolderName: String): Int

    @Query("update folders set description = :description where shortName = :id ")
    suspend fun updateDescription(id: String, description: String): Int

    @Query("select shortName, description, (select count(*) from expenses where expenses.folder_id = folders.id) as countRecords from folders")
    fun getAllExtendedView(): LiveData<List<FoldersExtendedView>>
*/
}