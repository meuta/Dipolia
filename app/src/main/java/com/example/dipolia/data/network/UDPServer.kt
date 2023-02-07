package com.example.dipolia.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.channels.DatagramChannel
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UDPServer {

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

    private val buffer = ByteArray(2048)
    private val port = 8002

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

//        thread {                //TODO: move threat creating from here!
//        val buffer = ByteArray(2048)
        var socket: DatagramSocket? = null



//        val datagramChannel = DatagramChannel.open()

        try {
            //Keep a socket open to listen to all the UDP traffic that is destined for this port
//              socket = DatagramSocket(port)           //Inappropriate blocking method call
            socket = openDatagramSocket(port)

            Log.d("UDPServer", "DatagramSocket($port) $socket ")
            socket.broadcast = true     //Enable/disable SO_BROADCAST.
            var packet = DatagramPacket(buffer, buffer.size - 1)
            //           socket.setSoTimeout(1000);   // set the timeout in milliseconds.
            Log.d("UDPServer", "DatagramPacket $packet ")
            var ok = true
            try {
                Log.d("UDPServer", "try ")

//                socket.receive(packet)            //Inappropriate blocking method call
                packet = receivePacket(socket, packet)
                Log.d("UDPServer", "ReceivedPacket $packet ")

            } catch (e: SocketTimeoutException) {
                ok = false
                socket.close()
                Log.i("UDPServer", "Socket timeout")
            }

            if (ok) {
//                Log.d("UDPServer", "Data $string ")

                var string = String(packet.data)
                Log.d("UDPServer", "String $string ")
                string = string.substring(0, packet.length)         //что делает эта строчка?
                Log.d("UDPServer", "substring $string ")

//                Handler(Looper.getMainLooper()).post {           // .post or .postDelay
//                    callback.invoke(string, packet.address)
//                }
                return Pair(string, packet.address)                 //will finally be executed??
            }

        } catch (e: Exception) {
            println("open fun receiveUDP catch exception." + e.toString())
            e.printStackTrace()
        } finally {
            socket?.close()
        }
        //For the callback calling on the main thread. Pass a runnable object:
//
//        }
        return null

    }

//                  (callback: (String, InetAddress) -> Unit) {
//    suspend fun receiveStringAndIPFromUDP(): Pair<String, InetAddress>? {
//    suspend fun receiveStringAndIPFromUDP() {


}
