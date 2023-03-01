package com.example.dipolia.data.network

import com.example.dipolia.domain.entities.LampType
import java.net.InetAddress

data class LampDto (

    val id: String,
    val ip: InetAddress,
    val lampType: LampType,
    var s1: String
)