package com.example.dipolia.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UDPServer {

    private val buffer = ByteArray(2048)
    private val port = 8002

    private fun openDatagramSocket(port: Int, callback: (DatagramSocket) -> Unit) {
        Handler(Looper.getMainLooper()).post {           // .post or .postDelay
            callback.invoke(DatagramSocket(port))
        }
    }

    private suspend fun openDatagramSocket(port: Int): DatagramSocket =
        suspendCoroutine { continuation ->
            openDatagramSocket(port) { datagramSocket ->
                continuation.resume(datagramSocket)
            }
        }


    private fun receivePacket(socket: DatagramSocket, packet: DatagramPacket, callback: (DatagramPacket) -> Unit) {
        socket.receive(packet)
        Log.d("UDPServer", "DatagramPacket $packet ")
        Handler(Looper.getMainLooper()).post {           // .post or .postDelay
            callback.invoke(packet)
        }
    }

    private suspend fun receivePacket(socket: DatagramSocket, packet: DatagramPacket): DatagramPacket =
        suspendCoroutine { continuation ->
            receivePacket(socket, packet) { datagramPacket ->
                continuation.resume(datagramPacket)
            }
        }

    suspend fun receiveStringAndIPFromUDP(): Pair<String, InetAddress>? {
        Log.d("UDPServer", "receiveStringAndIPFromUDP()")

        var socket: DatagramSocket? = null

        try {
            //Keep a socket open to listen to all the UDP traffic that is destined for this port
            socket = openDatagramSocket(port)

            Log.d("UDPServer", "DatagramSocket($port) $socket ")
            socket.broadcast = true     //Enable/disable SO_BROADCAST.
            var packet = DatagramPacket(buffer, buffer.size - 1)
            //           socket.setSoTimeout(1000);   // set the timeout in milliseconds.
            Log.d("UDPServer", "DatagramPacket $packet ")
            var ok = true
            try {
                Log.d("UDPServer", "try ")

                packet = receivePacket(socket, packet)
                Log.d("UDPServer", "ReceivedPacket $packet ")

            } catch (e: SocketTimeoutException) {
                ok = false
                socket.close()
                Log.i("UDPServer", "Socket timeout")
            }

            if (ok) {

                var string = String(packet.data)
                Log.d("UDPServer", "String $string ")
                string = string.substring(0, packet.length)         //что делает эта строчка?
                Log.d("UDPServer", "substring $string ")

                return Pair(string, packet.address)                 //will finally be executed??
            }

        } catch (e: Exception) {
            println("open fun receiveUDP catch exception." + e.toString())
            e.printStackTrace()
        } finally {
            socket?.close()
        }

        return null
    }
}

