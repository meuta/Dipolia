package com.example.dipolia.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.InetAddress

@Entity(tableName = "dipols")
data class DipolDbModel(
    @PrimaryKey
    val dipolId: String,
//    val dipolIp: InetAddress,
    val dipolIp: String,
    var r1: Double,
    var g1: Double,
    var b1: Double,
    var r2: Double,
    var g2: Double,
    var b2: Double
)
