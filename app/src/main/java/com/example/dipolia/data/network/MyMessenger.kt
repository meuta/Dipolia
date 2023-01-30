package com.example.dipolia.data.network

import java.net.InetAddress

open class MyMessenger {
    open fun send(string: String, ip: InetAddress){
        println(string +" mymes   "+ ip)
    }
}