package com.duncboi.realsquabble.ModelClasses

import android.util.Log

class Chat {
    private var sender: String = ""
    private var message: String = ""
    private var receiver: String = ""
    private var isseen = false
    private var url: String = ""
    private var messageId: String = ""
    private var timeOfMessage: String = ""
    private var uri: String = ""
    private var senderUsername: String = ""
    private var senderName: String = ""

    constructor()
    constructor(
        sender: String,
        message: String,
        receiver: String,
        isseen: Boolean,
        url: String,
        messageId: String,
        timeOfMessage: String,
        uri: String,
        senderUsername: String,
        senderName: String
    ) {
        this.sender = sender
        this.message = message
        this.receiver = receiver
        this.isseen = isseen
        this.url = url
        this.messageId = messageId
        this.timeOfMessage = timeOfMessage
        this.uri = uri
        this.senderUsername = senderUsername
        this.senderName = senderName
    }


    fun getSender(): String? {
        return sender
    }

    fun setSender(sender: String){
        this.sender = sender
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String){
        this.message = message
    }

    fun getReceiver(): String? {
        return receiver
    }

    fun setReceiver(receiver: String){
        this.receiver = receiver
    }

    fun getIsSeen(): Boolean {
        return isseen
    }

    fun setIsSeen(isseen: Boolean){
        this.isseen = isseen
    }

    fun getUrl(): String? {
        return url
    }

    fun setUrl(url: String){
        this.url = url
    }

    fun getMessageId(): String? {
        return messageId
    }

    fun setMessageId(messageId: String){
        this.messageId = messageId
    }

    fun getTimeOfMessage(): String? {
        return timeOfMessage
    }

    fun setTimeOfMessage(timeOfMessage: String){
        this.timeOfMessage = timeOfMessage
    }

    fun getUri(): String? {
        return uri
    }

    fun setUri(uri: String){
        this.uri = uri
    }

    fun getSenderUsername(): String? {
        return senderUsername
    }

    fun setSenderUsername(senderUsername: String){
        this.senderUsername = senderUsername
    }

    fun getSenderName(): String? {
        return senderName
    }

    fun setSenderName(senderName: String){
        this.senderName = senderName
    }



}