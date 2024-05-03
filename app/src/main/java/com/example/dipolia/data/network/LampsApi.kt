package com.example.dipolia.data.network

interface LampsApi {

    suspend fun fetchLampDto(): LampDto?
}