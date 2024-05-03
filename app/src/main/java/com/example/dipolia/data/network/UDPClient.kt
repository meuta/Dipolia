package com.example.dipolia.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class UDPClient @Inject constructor() {

    private val port = 8002

    private fun openDatagramSocket(callback: (DatagramSocket) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            callback.invoke(DatagramSocket())
        }
    }

    private suspend fun openDatagramSocket(): DatagramSocket =
        suspendCoroutine { continuation ->
            openDatagramSocket { datagramSocket ->
                continuation.resume(datagramSocket)
            }
        }


    private fun getInetAddressByName(host: String, callback: (InetAddress) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            callback.invoke(InetAddress.getByName(host))
        }
    }

    suspend fun getInetAddressByName(host: String): InetAddress =
        suspendCoroutine { continuation ->
            getInetAddressByName(host) { inetAddress ->
                continuation.resume(inetAddress)
            }
        }

    private fun sendPacket(
        socket: DatagramSocket,
        packet: DatagramPacket,
        callback: (DatagramPacket) -> Unit
    ) {
        socket.send(packet)
//        Log.d("UDPClient", "DatagramPacket $packet ")
        Handler(Looper.getMainLooper()).post {
            callback.invoke(packet)
        }
    }

    private suspend fun sendPacket(socket: DatagramSocket, packet: DatagramPacket): DatagramPacket =
        suspendCoroutine { continuation ->
            sendPacket(socket, packet) { datagramPacket ->
                continuation.resume(datagramPacket)
            }
        }

    suspend fun sendUDPSuspend(messageStr: String) {
//        Log.d("UDPClient", "message sendUDPSuspend($messageStr)")
        val socket = openDatagramSocket()
//        Log.d("UDPClient", "DatagramSocket($port) $socket ")

        socket.broadcast = true
        val outgoingData = messageStr.toByteArray()
        val outgoingPacket = DatagramPacket(
            outgoingData, outgoingData.size,
            getInetAddressByName("255.255.255.255"),
            port
        )
//        Log.d("UDPClient", "DatagramPacket $outgoingPacket ")

        try {
//            Log.d("UDPClient", "try ")
            sendPacket(socket, outgoingPacket)
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    suspend fun sendUDPSuspend(messageStr: String, ip: InetAddress) {

        try {
//            Log.d("UDPClient", "message to $ip sendUDPSuspend($messageStr)")
            val socket = openDatagramSocket()
//            Log.d("UDPClient", "DatagramSocket($port) $socket ")

            socket.broadcast = true
            val outgoingData = messageStr.toByteArray()
            val outgoingPacket = DatagramPacket(
                outgoingData, outgoingData.size,
                ip,
                port
            )
//            Log.d("UDPClient", "DatagramPacket $outgoingPacket ")

            try {
//                Log.d("UDPClient", "try ")
                sendPacket(socket, outgoingPacket)
            } catch (e: IOException) {
                //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
            }
        } catch (e: IOException) {
            Log.e("Q", "IOException: " + e.message)
        }
    }

}