package com.example.dipolia.data.mapper

import android.util.Log
import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.LampDbModel
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity

class DipoliaMapper {

    fun mapLampDtoToDbModel(lampDto: LampDto): LampDbModel {
//        Log.d("TestDipolDbModel", lampDto.id)
        return LampDbModel(
            lampDto.id,
            lampDto.ip.toString().substring(1),
            selected = false,
            lampType = lampDto.lampType,
            colorList = ColorList(emptyList<Double>())
        )
    }

    fun mapLampDbModelToEntity(lampDbModel: LampDbModel) = LampDomainEntity(
        id = lampDbModel.lampId,
        ip = lampDbModel.lampIp,
        lampType = lampDbModel.lampType,
        c = lampDbModel.colorList,
        selected = lampDbModel.selected
    ).also {
//        Log.d("mapLampDbModelToEntity", "$it")
    }

    fun mapLampDbModelToDipolEntity(lampDbModel: LampDbModel) : DipolDomainEntity {
        val c1: List<Double>
        val c2: List<Double>
        if (lampDbModel.colorList.colors.isEmpty()){
            c1 = listOf(0.0, 0.0, 0.0)
            c2 = listOf(0.0, 0.0, 0.0)
        } else{
            c1 = lampDbModel.colorList.colors.subList(0, 3)
            c2 = lampDbModel.colorList.colors.subList(3, 6)
        }

        return DipolDomainEntity(
            id = lampDbModel.lampId,
            ip = lampDbModel.lampIp,
            c1 = c1,
            c2 = c2,
            selected = lampDbModel.selected
        ).also {
//            Log.d("mapLampDbModelToDipolEntity", "$it")
        }
    }

    fun mapLampDbModelToFiveLightsEntity(lampDbModel: LampDbModel) : FiveLightsDomainEntity {
        val c: List<Double> = if (lampDbModel.colorList.colors.isEmpty()){
            listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        } else{
            lampDbModel.colorList.colors.subList(0, 5)
        }

        return FiveLightsDomainEntity(
            id = lampDbModel.lampId,
            ip = lampDbModel.lampIp,
            c = c,
            selected = lampDbModel.selected
        ).also {
//            Log.d("mapLampDbModelToFiveLightsEntity", "$it")
        }
    }

    fun mapLampDtoToEntity(lampDto: LampDto): LampDomainEntity {
        val lampDomainEntity = LampDomainEntity(
            id = lampDto.id,
            lampDto.ip.toString().substring(1),
            selected = false,
            lampType = lampDto.lampType,
            c = ColorList(emptyList()),
            lastConnection = lampDto.lastConnection
        )
        return lampDomainEntity

    }

}