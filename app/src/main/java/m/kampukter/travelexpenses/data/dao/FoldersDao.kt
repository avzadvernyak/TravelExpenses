package m.kampukter.travelexpenses.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import m.kampukter.travelexpenses.data.Folders
import m.kampukter.travelexpenses.data.FoldersExtendedView

@Dao
interface FoldersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expensesProfiles: List<Folders>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFolder(folders: Folders)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(expensesProfiles: Folders)

    @Query("select * from folders")
    fun getAllFoldersFlow(): Flow<List<Folders>>

    @Query("select * from folders where id = :idFolder")
    fun searchById(idFolder: Long): Flow<Folders>

    @Query("select id, shortName, description, (select count(*) from expenses where expenses.folder_id = folders.id) as countRecords from folders")
    fun getAllExtendedViewFlow(): Flow<List<FoldersExtendedView>>

    @Query("delete from folders WHERE folders.id = :folderId")
    suspend fun deleteFolderByName( folderId: Long)

    @Query("update folders set shortName = :newFolderName where id = :id ")
    suspend fun updateShortName(id: Long, newFolderName: String): Int

    @Query("update folders set description = :description where id = :id ")
    suspend fun updateDescription(id: Long, description: String): Int

    @Update
    suspend fun update( folder: Folders)
}