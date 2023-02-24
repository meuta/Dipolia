package com.example.dipolia.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dipols")
data class DipolDbModel(
    @PrimaryKey
    val dipolId: String,
    val dipolIp: String,
    var selected: Boolean = false,
    var connected: Boolean = false,
    var lastConnection: Long = 0,
    var r1: Double = 0.0,
    var g1: Double = 0.0,
    var b1: Double = 0.0,
    var r2: Double = 0.0,
    var g2: Double = 0.0,
    var b2: Double = 0.0
//    var colorSet: List<List<Double>> = listOf(listOf(0.0))
//    var colorSet: MutableList<Double> = mutableListOf(0.0)
)

