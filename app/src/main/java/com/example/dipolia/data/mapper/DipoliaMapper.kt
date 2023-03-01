package com.example.dipolia.data.mapper

import android.util.Log
import com.example.dipolia.data.database.ColorList
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.database.FiveLightsDbModel
import com.example.dipolia.data.database.LampDbModel
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.data.network.LampDto
import com.example.dipolia.domain.DipolDomainEntity
import com.example.dipolia.domain.entities.FiveLightsDomainEntity
import com.example.dipolia.domain.entities.LampDomainEntity

class DipoliaMapper {

    fun mapDtoToDbModel(dipolDto: DipolDto): DipolDbModel {
        Log.d("TestDipolDbModel", dipolDto.id)
        return DipolDbModel(
            dipolDto.id,
            dipolDto.ip.toString().substring(1),
            selected = false,
            connected = true,

        )
    }

    fun mapLampDtoToDbModel(lampDto: LampDto): LampDbModel {
        Log.d("TestDipolDbModel", lampDto.id)
        return LampDbModel(
            lampDto.id,
            lampDto.ip.toString().substring(1),
            selected = false,
            connected = true,
            lampType = lampDto.lampType,
            colorList = ColorList(emptyList<Double>())
        )
    }

    fun mapFiveLightsDtoToDbModel(fiveLightsDto: DipolDto): FiveLightsDbModel {
        Log.d("TestDipolDbModel", fiveLightsDto.id)
        return FiveLightsDbModel(
            fiveLightsDto.id,
            fiveLightsDto.ip.toString().substring(1),
            selected = false,
            connected = true
        )
    }

    fun mapDbModelToEntity(dipolDbModel: DipolDbModel) = DipolDomainEntity(
        id = dipolDbModel.dipolId,
        ip = dipolDbModel.dipolIp,
        c1 = listOf(dipolDbModel.r1, dipolDbModel.g1, dipolDbModel.b1),
        c2 = listOf(dipolDbModel.r2, dipolDbModel.g2, dipolDbModel.b2),
        selected = dipolDbModel.selected,
        connected = dipolDbModel.connected
    ).also {
        Log.d("mapDbModelToEntity", "$it")
    }

    fun mapLampDbModelToEntity(lampDbModel: LampDbModel) = LampDomainEntity(
        id = lampDbModel.lampId,
        ip = lampDbModel.lampIp,
        lampType = lampDbModel.lampType,
        c = lampDbModel.colorList,
        selected = lampDbModel.selected,
        connected = lampDbModel.connected
    ).also {
        Log.d("mapLampDbModelToEntity", "$it")
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
            selected = lampDbModel.selected,
            connected = lampDbModel.connected
        ).also {
            Log.d("mapLampDbModelToDipolEntity", "$it")
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
            selected = lampDbModel.selected,
            connected = lampDbModel.connected
        ).also {
            Log.d("mapLampDbModelToFiveLightsEntity", "$it")
        }
    }

    fun mapFiveLightsDbModelToEntity(fiveLightsDbModel: FiveLightsDbModel) = FiveLightsDomainEntity(
        id = fiveLightsDbModel.fiveLightsId,
        ip = fiveLightsDbModel.fiveLightsIp,
        c = listOf(fiveLightsDbModel.r, fiveLightsDbModel.g, fiveLightsDbModel.b, fiveLightsDbModel.w, fiveLightsDbModel.uv),
        selected = fiveLightsDbModel.selected,
        connected = fiveLightsDbModel.connected
    ).also {
        Log.d("mapFiveLightsDbModelToEntity", "$it")
    }

    //
//    fun mapEntityToDbModel(dipolDomainEntity: DipolDomainEntity) = DipolDbModel(
//        dipolId = dipolDomainEntity.id,
//        dipolIp = dipolDomainEntity.ip,
//        r1 = dipolDomainEntity.c1[0],
//        g1 = dipolDomainEntity.c1[1],
//        b1 = dipolDomainEntity.c1[2],
//        r2 = dipolDomainEntity.c2[0],
//        g2 = dipolDomainEntity.c2[1],
//        b2 = dipolDomainEntity.c2[2]
//    )
//
    fun mapListDbModelToEntity(list: List<DipolDbModel>) =
        list.map { mapDbModelToEntity(it) }.also {
            Log.d("mapListDbModelToEntity", "$it")
        }
//
//    fun mapDbModelToDto(dipolDbModel: DipolDbModel): DipolDto {
//        var rabbitColorSpeed = 0.5
//
//        val r1 = (BigDecimal(dipolDbModel.r1).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val g1 = (BigDecimal(dipolDbModel.g1).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val b1 = (BigDecimal(dipolDbModel.b1).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val r2 = (BigDecimal(dipolDbModel.r2).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val g2 = (BigDecimal(dipolDbModel.g2).setScale(3, RoundingMode.HALF_DOWN)).toString()
//        val b2 = (BigDecimal(dipolDbModel.b2).setScale(3, RoundingMode.HALF_DOWN)).toString()
//
//        val rcs = (BigDecimal(rabbitColorSpeed).setScale(3, RoundingMode.HALF_DOWN)).toString()
//
//        val s1: String = "r1=" + r1 + ";g1=" + g1 + ";b1=" + b1 +
//                ";r2=" + r2 + ";g2=" + g2 + ";b2=" + b2 + ";rcs=" + rcs
//
//        return DipolDto(id = dipolDbModel.dipolId, ip = dipolDbModel.dipolIp, s1 = s1)
//    }
}