package com.example.dipolia.domain.entities

data class FiveLightsDomainEntity (
    val id: String,
    val ip: String,

    var c: List<Double>,

    var lc: List<Double?>? = null,
    var last_set_c: List<Double?>? = null,

    var selected: Boolean = false,
    var connected : Boolean = false

)