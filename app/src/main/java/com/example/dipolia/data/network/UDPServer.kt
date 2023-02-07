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
import kotlin.concurrent.thread


class UDPServer {

    //    fun receiveStringAndIPFromUDP(callback: (String, InetAddress) -> Unit) {
//    suspend fun receiveStringAndIPFromUDP(): Pair<String, InetAddress>? {
    suspend fun receiveStringAndIPFromUDP() {
        Log.d("UDPServer", "receiveStringAndIPFromUDP()")

//        thread {                //TODO: move threat creating from here!
        val buffer = ByteArray(2048)
        var socket: DatagramSocket? = null

        val port = 8002

        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager)
            .udp()
//             ?    is it possible write the following:     ?
            .bind(InetSocketAddress("localhost", port)) {
                broadcast = true
                receiveBufferSize = 2048
            }
        val localAddress = serverSocket.localAddress
        Log.d("UDPServer", "Server is listening at $localAddress")

        serverSocket.use {
            Log.d("UDPServer", "serverSocket.use $it")
            val receiveChannel = it.openReadChannel()
            Log.d("UDPServer", "receiveChannel $receiveChannel")
            while (true) {
                Log.d("UDPServer", "true")
                try {
                    Log.d("UDPServer", "try")
                    val datagram = it.receive()
                    Log.d("UDPServer", "Datagram received $datagram")
                    val packet = datagram.packet
                    Log.d("UDPServer", "Packet received $packet")
                    val line = packet.readText()
                    Log.d("UDPServer", "line = packet.readText() $line")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

//        try {
//            //Keep a socket open to listen to all the UDP traffic that is destined for this port
//            socket = DatagramSocket(port)            //Inappropriate blocking method call
//            socket.broadcast = true     //Enable/disable SO_BROADCAST.
//            val packet = DatagramPacket(buffer, buffer.size - 1)
//            //           socket.setSoTimeout(1000);   // set the timeout in milliseconds.
//            var ok = true
//            try {
//                socket.receive(packet)            //Inappropriate blocking method call
//
//            } catch (e: SocketTimeoutException) {
//                ok = false
//                socket.close()
//                Log.i(" socket", "timeout")
//            }
//
//            if (ok) {
//                var string = String(packet.data)
//                string = string.substring(0, packet.length)         //что делает эта строчка?
//
////                    Handler(Looper.getMainLooper()).post {           // .post or .postDelay
////                        callback.invoke(string, packet.address)
////                    }
//                return Pair(string, packet.address)                 //will finally be executed??
//            }
//
//        } catch (e: Exception) {
//            println("open fun receiveUDP catch exception." + e.toString())
//            e.printStackTrace()
//        } finally {
//            socket?.close()
//        }
        //For the callback calling on the main thread. Pass a runnable object:

//        }
//        return null

    }

}

