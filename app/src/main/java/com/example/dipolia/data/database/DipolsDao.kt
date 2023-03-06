package com.example.dipolia.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.dipolia.domain.entities.LampType

@Dao
interface DipolsDao {

    @Query("SELECT * FROM lamps")
    fun getLampsTable(): LiveData<List<LampDbModel>>

//    @Query("SELECT * FROM lamps WHERE lampType = :lampType AND connected")
//    fun getConnectedLampsListByTypeLD(lampType: LampType): LiveData<List<LampDbModel>>
    @Query("SELECT * FROM lamps WHERE lampType = :lampType")
    fun getConnectedLampsListByTypeLD(lampType: LampType): LiveData<List<LampDbModel>>

    @Query("SELECT * FROM lamps WHERE lampType = :lampType LIMIT 1")
    fun getFiveLightsByTypeLD(lampType: LampType): LiveData<LampDbModel>

//    @Query("SELECT * FROM lamps WHERE connected")
//    fun getConnectedLampsListLD(): LiveData<List<LampDbModel>>
    @Query("SELECT * FROM lamps")
    fun getConnectedLampsListLD(): LiveData<List<LampDbModel>>

    @Query("SELECT * FROM dipols WHERE lastConnection < strftime('%s','now') - 5 ")
    fun getNotConnectedDipolList(): List<DipolDbModel>

    @Query("SELECT * FROM fiveLights WHERE lastConnection < strftime('%s','now') - 5 ")
    fun getNotConnectedFiveLight(): FiveLightsDbModel?

    @Query("SELECT * FROM lamps WHERE lastConnection < strftime('%s','now') - 5 ")
    fun getNotConnectedLampsList(): List<LampDbModel>

    @Query("SELECT * FROM dipols")
    fun getDipolList(): List<DipolDbModel>

    @Query("SELECT * FROM lamps")
    fun getLampsList(): List<LampDbModel>

    @Query("SELECT * FROM dipols WHERE dipolId=:dipolItemId LIMIT 1")
    fun getDipolItemById(dipolItemId: String): DipolDbModel?

    @Query("SELECT * FROM lamps WHERE lampId=:lampId LIMIT 1")
    fun getLampItemById(lampId: String): LampDbModel?

    @Query("SELECT * FROM fiveLights WHERE fiveLightsId=:fiveLightsItemId LIMIT 1")
    fun getFiveLightsItemById(fiveLightsItemId: String): FiveLightsDbModel?

    @Query("SELECT * FROM dipols WHERE selected=:selected LIMIT 1")
    fun getSelectedDipolItem(selected: Boolean): DipolDbModel?

    @Query("SELECT * FROM lamps WHERE selected=:selected LIMIT 1")
    fun getLampSelectedItem(selected: Boolean): LampDbModel?

//    @Query("SELECT * FROM lamps WHERE selected=:selected and connected=:connected LIMIT 1")
//    fun getLampSelectedConnectedItem(selected: Boolean, connected: Boolean): LampDbModel?
    @Query("SELECT * FROM lamps WHERE selected=:selected LIMIT 1")
    fun getLampSelectedConnectedItem(selected: Boolean): LampDbModel?

//    @Query("SELECT * FROM lamps WHERE selected=:selected and connected=:connected LIMIT 1")
//    fun getLampSelectedConnectedItemLD(selected: Boolean, connected: Boolean): LiveData<LampDbModel?>
    @Query("SELECT * FROM lamps WHERE selected=:selected LIMIT 1")
    fun getLampSelectedConnectedItemLD(selected: Boolean): LiveData<LampDbModel?>

    @Query("SELECT * FROM lamps WHERE lampType=:lampType AND selected=:selected LIMIT 1")
    fun getSelectedDipolItemLD(selected: Boolean, lampType: LampType): LiveData<LampDbModel?>

    @Query("SELECT * FROM fiveLights LIMIT 1")
    fun getFiveLightsItemLD(): LiveData<FiveLightsDbModel?>

    @Query("SELECT * FROM lamps WHERE selected=:selected LIMIT 1")
    fun getLampSelectedItemLD(selected: Boolean): LiveData<LampDbModel?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)        //If we add an item with existed ID, it will be replace, so we can use it also in the edit case
    fun addDipolItem(dipolItemBbModel: DipolDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFiveLightsItem(fiveLightsDbModel: FiveLightsDbModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLampItem(lampDbModel: LampDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateDipolItem(dipolItemBbModel: DipolDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateFiveLightsItem(fiveLightsDbModel: FiveLightsDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateLampItem(lampDbModel: LampDbModel): Int

}
