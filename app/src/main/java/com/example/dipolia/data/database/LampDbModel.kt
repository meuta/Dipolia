package com.example.dipolia.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dipolia.domain.entities.LampType

@Entity(tableName = "lamps")
data class LampDbModel (

    @PrimaryKey
    val lampId: String,
//    var selected: Boolean = false,
    var lastConnection: Long = 0,
    val lampType: LampType = LampType.UNKNOWN_LAMP_TYPE,
    var colorList: ColorList
)