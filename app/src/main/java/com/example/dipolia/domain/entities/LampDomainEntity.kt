package com.example.dipolia.domain.entities

import com.example.dipolia.data.database.ColorList

data class LampDomainEntity(
    val id: String,
    val ip: String,
    val lampType: LampType,
    var c: ColorList,

    var lc: List<Double?>? = null,
    var last_set_c: List<Double?>? = null,

    var selected: Boolean = false,
    var connected: Boolean = false

)
