package com.example.dipolia.data.database

import androidx.room.*

@Dao
interface DipolsDao {

//    @Query("SELECT * FROM lamps")
//    fun getLampsTable(): List<LampDbModel>

    @Query("SELECT * FROM lamps WHERE lampId=:lampId LIMIT 1")
    fun getLampItemById(lampId: String): LampDbModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLampItem(lampDbModel: LampDbModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateLampItem(lampDbModel: LampDbModel): Int

}
