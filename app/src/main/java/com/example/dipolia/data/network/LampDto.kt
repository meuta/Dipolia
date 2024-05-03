package com.example.dipolia.data.network

import com.example.dipolia.domain.entities.LampType
import java.net.InetAddress

data class LampDto(

    val id: String,
    val ip: InetAddress,
    val lampType: LampType,
    var lastConnection: Long
) {
    val connected: Boolean
//        get() = lastConnection > System.currentTimeMillis() / 1000 - 20
        get() = lastConnection > System.currentTimeMillis() / 1000 - 50
}