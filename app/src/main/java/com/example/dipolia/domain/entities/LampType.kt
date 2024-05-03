package com.example.dipolia.domain.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LampType: Parcelable {
    DIPOL, FIVE_LIGHTS, UNKNOWN_LAMP_TYPE
}
