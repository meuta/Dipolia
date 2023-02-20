package com.example.dipolia.data.mapper

import android.util.Log
import com.example.dipolia.data.database.DipolDbModel
import com.example.dipolia.data.network.DipolDto
import com.example.dipolia.domain.DipolDomainEntity

class DipoliaMapper {

    fun mapDtoToDbModel(dipolDto: DipolDto): DipolDbModel {
        Log.d("TestDipolDbModel", dipolDto.id)
        return DipolDbModel(
            dipolDto.id,
            dipolDto.ip.toString().substring(1),
//            0.0,
//            0.0,
//            0.0,
//            0.0,
//            0.0,
//            0.0
            selected = false,
            connected = true
        )
    }

    fun mapDbModelToEntity(dipolDbModel: DipolDbModel) = DipolDomainEntity(
        id = dipolDbModel.dipolId,
        ip = dipolDbModel.dipolIp,
        c1 = listOf(dipolDbModel.r1, dipolDbModel.g1, dipolDbModel.b1),
        c2 = listOf(dipolDbModel.r2, dipolDbModel.g2, dipolDbModel.b2),
//        c1 = dipolDbModel.colorSet[0],
//        c2 = dipolDbModel.colorSet[0],
        selected = dipolDbModel.selected
    ).also {
        Log.d("mapDbModelToEntity", "$it")
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