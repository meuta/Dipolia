package com.example.dipolia.domain.entities

data class FiveLightsDomainEntity(

    val id: String,
    val ip: String,

    var c: List<Double>,

    var lc: List<Double?>? = null,
    var last_set_c: List<Double?>? = null,

    var selected: Boolean = false,
    var lastConnection: Long = 0,

    var lampName: String? = null
) {

    val connected: Boolean
//        get() = lastConnection > System.currentTimeMillis() / 1000 - 20
        get() = lastConnection > System.currentTimeMillis() / 1000 - 50

    val currentLampName = lampName ?: id
}