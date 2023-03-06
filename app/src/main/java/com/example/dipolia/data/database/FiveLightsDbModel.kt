package com.example.dipolia.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fiveLights")
data class FiveLightsDbModel(
    @PrimaryKey
    val fiveLightsId: String,
    val fiveLightsIp: String,
    var selected: Boolean = false,
    var lastConnection: Long = 0,
    var r: Double = 0.0,
    var g: Double = 0.0,
    var b: Double = 0.0,
    var w: Double = 0.0,
    var uv: Double = 0.0
)
