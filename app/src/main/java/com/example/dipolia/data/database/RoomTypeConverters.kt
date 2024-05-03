package com.example.dipolia.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson

class RoomTypeConverters {

    @TypeConverter
    fun convertColorListToJSONString(colorList: ColorList): String = Gson().toJson(colorList)

    @TypeConverter
    fun convertJSONStringToColorList(jsonString: String): ColorList = Gson().fromJson(
        jsonString,
        ColorList::class.java
    )

}