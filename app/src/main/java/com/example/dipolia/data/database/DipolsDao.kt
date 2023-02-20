package com.example.dipolia.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DipolsDao {

    @Query("SELECT * FROM dipols WHERE connected=:connected")
    fun getDipolListLD(connected: Boolean): LiveData<List<DipolDbModel>>

    @Query("SELECT * FROM dipols")
    fun getDipolList(): List<DipolDbModel>

    @Query("SELECT * FROM dipols WHERE dipolId=:dipolItemId LIMIT 1")
    fun getDipolItemById(dipolItemId: String): DipolDbModel?

    @Query("SELECT * FROM dipols WHERE selected=:selected LIMIT 1")
    fun getSelectedDipolItem(selected: Boolean): DipolDbModel?

    @Query("SELECT * FROM dipols WHERE selected=:selected LIMIT 1")
    fun getSelectedDipolItemLD(selected: Boolean): LiveData<DipolDbModel?>

//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    fun updateDipolItem(dipolItemBbModel: DipolDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)        //If we add an item with existed ID, it will be replace, so we can use it also in the edit case
    fun addDipolItem(dipolItemBbModel: DipolDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDipolItem(dipolItemBbModel: DipolDbModel)

//    @Query("")

//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    fun refreshConnectedList(dipolListBbModel: List<DipolDbModel>)
}
