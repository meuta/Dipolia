package com.example.dipolia.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dipols")
data class DipolDbModel(
    @PrimaryKey
    val dipolId: String,
    val dipolIp: String,
    var selected: Boolean,
    var r1: Double = 0.0,
    var g1: Double = 0.0,
    var b1: Double = 0.0,
    var r2: Double = 0.0,
    var g2: Double = 0.0,
    var b2: Double = 0.0
)
