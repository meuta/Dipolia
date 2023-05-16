package com.example.dipolia.domain.entities


data class DipolDomainEntity(

    val id: String,
    val ip: String,

    var c1: List<Double>,
    var c2: List<Double>,

    var lc1: List<Double?>? = null,
    var lc2: List<Double?>? = null,
    var last_set_c1: List<Double?>? = null,
    var last_set_c2: List<Double?>? = null,

    var selected: Boolean = false,
    var lastConnection: Long = 0,

    var lampName: String? = null
) {

    val connected: Boolean
        get() = lastConnection > System.currentTimeMillis() / 1000 - 20

    val currentLampName = lampName ?: id
}
