package com.example.dipolia.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DipolsDao {

    @Query("SELECT * FROM dipols WHERE connected")
    fun getConnectedDipolListLD(): LiveData<List<DipolDbModel>>

    @Query("SELECT * FROM dipols WHERE lastConnection < strftime('%s','now') - 5 ")
    fun getNotConnectedDipolList(): List<DipolDbModel>

    @Query("SELECT * FROM dipols")
    fun getDipolList(): List<DipolDbModel>

    @Query("SELECT * FROM dipols WHERE dipolId=:dipolItemId LIMIT 1")
    fun getDipolItemById(dipolItemId: String): DipolDbModel?

    @Query("SELECT * FROM fiveLights WHERE fiveLightsId=:fiveLightsItemId LIMIT 1")
    fun getFiveLightsItemById(fiveLightsItemId: String): FiveLightsDbModel?

    @Query("SELECT * FROM dipols WHERE selected=:selected LIMIT 1")
    fun getSelectedDipolItem(selected: Boolean): DipolDbModel?

    @Query("SELECT * FROM dipols WHERE selected=:selected LIMIT 1")
    fun getSelectedDipolItemLD(selected: Boolean): LiveData<DipolDbModel?>

    @Query("SELECT * FROM fiveLights LIMIT 1")
    fun getFiveLightsItemLD(): LiveData<FiveLightsDbModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)        //If we add an item with existed ID, it will be replace, so we can use it also in the edit case
    fun addDipolItem(dipolItemBbModel: DipolDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)        //If we add an item with existed ID, it will be replace, so we can use it also in the edit case
    fun addFiveLightsItem(fiveLightsDbModel: FiveLightsDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDipolItem(dipolItemBbModel: DipolDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateFiveLightsItem(fiveLightsDbModel: FiveLightsDbModel)

}
