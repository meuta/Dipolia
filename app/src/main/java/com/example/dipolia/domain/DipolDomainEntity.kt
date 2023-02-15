package com.example.dipolia.domain

import java.net.InetAddress

data class DipolDomainEntity(
    val id: String,
//    val ip: InetAddress,
    val ip: String,

    var c1: List<Double>,
    var c2: List<Double>,

    var lc1: List<Double?>? = null,
    var lc2: List<Double?>? = null,
    var last_set_c1: List<Double?>? = null,
    var last_set_c2: List<Double?>? = null,

    var selected: Boolean = false

)
