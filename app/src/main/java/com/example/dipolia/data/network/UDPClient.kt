package com.example.dipolia.data.network

import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UDPClient {

    private val port = 8002

    fun sendUDP(messageStr: String) {
        // Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            //Open a port to send the package
            val socket = DatagramSocket()
            socket.broadcast = true
            val sendData = messageStr.toByteArray()
            val sendPacket = DatagramPacket(
                sendData, sendData.size,
                InetAddress.getByName("255.255.255.255"),
                //    InetAddress.getByName(Settings.RemoteHost),
                port
            )
            socket.send(sendPacket)
            //      InetAddress.getByName(Settings.RemoteHost) + ":" + Settings.RemotePort)
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    private fun openDatagramSocket(callback: (DatagramSocket) -> Unit) {
        Handler(Looper.getMainLooper()).post {           // .post or .postDelay
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
        Handler(Looper.getMainLooper()).post {           // .post or .postDelay
            callback.invoke(InetAddress.getByName(host))
        }
    }

    private suspend fun getInetAddressByName(host: String): InetAddress =
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
        Log.d("UDPClient", "DatagramPacket $packet ")
        Handler(Looper.getMainLooper()).post {           // .post or .postDelay
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
        Log.d("UDPClient", "sendUDPSuspend($messageStr)")
//            val socket = DatagramSocket()
        val socket = openDatagramSocket()
        Log.d("UDPClient", "DatagramSocket($port) $socket ")

        socket.broadcast = true
        val outgoingData = messageStr.toByteArray()
        val outgoingPacket = DatagramPacket(
            outgoingData, outgoingData.size,
            getInetAddressByName("255.255.255.255"),
            port
        )
        Log.d("UDPClient", "DatagramPacket $outgoingPacket ")

        try {
            Log.d("UDPClient", "try ")
            sendPacket(socket, outgoingPacket)
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }


    fun sendUDP(messageStr: String, ip: InetAddress) {
        // Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
//        println("Sending")

        try {
            //Open a port to send the package
            val socket = DatagramSocket()
            socket.broadcast = false
            val sendData = messageStr.toByteArray()
            val sendPacket = DatagramPacket(
                sendData, sendData.size,
                ip,
                //    InetAddress.getByName(Settings.RemoteHost),
                port
            )
            socket.send(sendPacket)
            // println("fun sendBroadcast: packet sent to: " +
            //      InetAddress.getByName(Settings.RemoteHost) + ":" + Settings.RemotePort)
        } catch (e: IOException) {
            Log.e("Q", "IOException: " + e.message)
        }
    }

}