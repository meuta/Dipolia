package com.example.dipolia.data.mapper

import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.LampDbModel
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.domain.entities.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity

class DipoliaMapper {

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

    fun mapLampDtoToDbModel(lampDto: LampDto): LampDbModel {
//        Log.d("TestDipolDbModel", lampDto.id)
        return LampDbModel(
            lampDto.id,
//            lampDto.ip.toString().substring(1),
//            selected = false,
            lampType = lampDto.lampType,
            colorList = ColorList(emptyList<Double>())
        )
    }

    fun mapLampEntityToDipolEntity(lampDomainEntity: LampDomainEntity) : DipolDomainEntity {
        val c1: List<Double>
        val c2: List<Double>
        if (lampDomainEntity.c.colors.isEmpty()){
            c1 = listOf(0.0, 0.0, 0.0)
            c2 = listOf(0.0, 0.0, 0.0)
        } else{
            c1 = lampDomainEntity.c.colors.subList(0, 3)
            c2 = lampDomainEntity.c.colors.subList(3, 6)
        }

        return DipolDomainEntity(
            id = lampDomainEntity.id,
            ip = lampDomainEntity.ip,
            c1 = c1,
            c2 = c2,
            selected = lampDomainEntity.selected,
            lastConnection = lampDomainEntity.lastConnection
        ).also {
//            Log.d("mapLampDbModelToDipolEntity", "$it")
        }
    }

    fun mapLampEntityToFiveLightsEntity(lampDomainEntity: LampDomainEntity) : FiveLightsDomainEntity {
        val c: List<Double>
        c = if (lampDomainEntity.c.colors.isEmpty()){
            listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        } else{
            lampDomainEntity.c.colors.subList(0, 5)
        }

        return FiveLightsDomainEntity(
            id = lampDomainEntity.id,
            ip = lampDomainEntity.ip,
            c = c,
//            selected = lampDbModel.selected
            selected = lampDomainEntity.selected,
            lastConnection = lampDomainEntity.lastConnection
        ).also {
//            Log.d("mapLampDbModelToDipolEntity", "$it")
        }
    }

    fun mapLampDbModelToEntity(lampDbModel: LampDbModel) = LampDomainEntity(
        id = lampDbModel.lampId,
        ip = "",
        lampType = lampDbModel.lampType,
        c = lampDbModel.colorList,
//        selected = lampDbModel.selected
        selected = false
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
            ip = "",
            c1 = c1,
            c2 = c2,
//            selected = lampDbModel.selected
            selected = false
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
            ip = "",
            c = c,
//            selected = lampDbModel.selected
            selected = false
        ).also {
//            Log.d("mapLampDbModelToFiveLightsEntity", "$it")
        }
    }

}