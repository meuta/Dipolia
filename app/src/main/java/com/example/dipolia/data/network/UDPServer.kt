package com.example.dipolia.data.network

import com.example.dipolia.data.network.MyMessenger
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.dipolia.MainActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlin.concurrent.thread


class UDPServer {
//    var onMessageReceive: MyMessenger
//    val threadWithRunnable = Thread(UdpDataArrival())

//    init {
//        onMessageReceive= MyMessenger()
////        println("Starting UDP server.")
////        threadWithRunnable.start()
//    }

//    fun stop(){
//        Log.i("RECEIVER", "STOP")
//
////        threadWithRunnable.interrupt()
//        Log.i("RECEIVER", "WAIT")
//
//        Log.i("RECEIVER", "done")
//
//    }

    fun receiveStringAndIPFromUDP(callback: (String, InetAddress) -> Unit) {
        thread {                //TODO: move threat creating from here!
            val buffer = ByteArray(2048)
            var socket: DatagramSocket? = null

            val port = 8002
            try {
                //Keep a socket open to listen to all the UDP traffic that is destined for this port
                socket = DatagramSocket(port)
                socket.broadcast = true     //Enable/disable SO_BROADCAST.
                val packet = DatagramPacket(buffer, buffer.size-1)
                //           socket.setSoTimeout(1000);   // set the timeout in milliseconds.
                var ok = true
                try {
                    socket.receive(packet)

                } catch (e: SocketTimeoutException) {
                    ok = false
                    socket.close()
                    Log.i(" socket", "timeout")
                }
//                if(!ok){
//                    Thread.currentThread().interrupt()
//                }
                if (ok) {
                    var string = String(packet.data)
                    string = string.substring(0, packet.length)         //что делает эта строчка?
//                println("open fun receiveUDP packet received = " + string)
//                println("ip = " + packet.address.toString())



//                val handler = Handler(Looper.getMainLooper())
//                // post a Runnable to the main Thread
//                handler.post {
//                    onMessageReceive.send(string, packet.address)
//                }
                    Handler(Looper.getMainLooper()).post {           // .post or .postDelay
                        callback.invoke(string, packet.address)
                    }
                }



            } catch (e: Exception) {
                println("open fun receiveUDP catch exception." + e.toString())
                e.printStackTrace()
            } finally {
                socket?.close()
            }
            //For the callback calling on the main thread. Pass a runnable object:

        }
    }


//    inner class UdpDataArrival: Runnable/*, MainActivity()*/ {
//        override fun run() {
////            println("${Thread.currentThread()} Runnable Thread Started.")
//            while (!Thread.currentThread().isInterrupted) {
//                //while (!threadWithRunnable.isInterrupted){
//                receiveUDP()
////                Log.i("RECEIVER", "still processing")
//
//            }
//            Log.i("RECEIVER", "Final STOP")
//
//        }
//
//        open fun receiveUDP() {
//            val buffer = ByteArray(2048)
//            var socket: DatagramSocket? = null
//
//            val port = 8002
//            try {
//                //Keep a socket open to listen to all the UDP traffic that is destined for this port
//                socket = DatagramSocket(port)
//                socket.broadcast = true     //Enable/disable SO_BROADCAST.
//                val packet = DatagramPacket(buffer, buffer.size-1)
//                //           socket.setSoTimeout(1000);   // set the timeout in milliseconds.
//                var ok = true
//                try {
//                    socket.receive(packet)
//
//                } catch (e: SocketTimeoutException) {
//                    ok = false
//                    socket.close()
//                    Log.i(" socket", "timeout")
//                }
//                if(!ok){
//                    return
//                }
//                var string = String(packet.data)
//                string = string.substring(0, packet.length)
////                println("open fun receiveUDP packet received = " + string)
////                println("ip = " + packet.address.toString())
//
//
//
////                val handler = Handler(Looper.getMainLooper())
////                // post a Runnable to the main Thread
////                handler.post {
////                    onMessageReceive.send(string, packet.address)
////                }
//
//
//
//            } catch (e: Exception) {
//                println("open fun receiveUDP catch exception." + e.toString())
//                e.printStackTrace()
//            } finally {
//                socket?.close()
//            }
//        }
//    }
}


