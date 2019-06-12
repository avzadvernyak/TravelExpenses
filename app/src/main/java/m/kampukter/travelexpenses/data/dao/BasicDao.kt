package m.kampukter.travelexpenses.data.dao

import androidx.room.*

@Dao
interface BasicDao<in T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: T): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<T>)
    @Delete
    suspend fun delete(type : T)
    @Update
    suspend fun update(type : T)
}