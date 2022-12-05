package com.example.driftdb

class Messages {
    var message: String? = null
    var senderNumber: String? = null
    var msgTime: String? = null

    constructor(){}

    constructor(message: String?, senderNumber: String?, msgTime: String?){
        this.message = message
        this.senderNumber = senderNumber
        this.msgTime = msgTime
    }
}