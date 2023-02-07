package com.example.dipolia.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DipolsDao {

//    @Query("SELECT * FROM dipols")
//    fun getDipolList(): LiveData<List<DipolDbModel>>
//
//    @Query("SELECT * FROM dipols WHERE dipolId=:dipolItemId LIMIT 1")
//    fun getDipolItem(dipolItemId: String): DipolDbModel
//
//    @Update(onConflict = OnConflictStrategy.REPLACE)
//    fun updateDipolItem(dipolItemBbModel: DipolDbModel)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)        //If we add an item with existed ID, it will be replace, so we can use it also in the edit case
//    fun addDipolItem(dipolItemBbModel: DipolDbModel)
}
