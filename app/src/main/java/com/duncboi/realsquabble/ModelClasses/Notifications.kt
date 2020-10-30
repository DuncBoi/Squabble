package com.duncboi.realsquabble.ModelClasses

class Notifications {

    private var uri: String = ""
    private var message: String = ""
    private var id: String = ""
    private var timeOfPost: String = ""
    private var senderId: String = ""
    private var groupId: String = ""
    private var commentId: String = ""

    constructor()
    constructor(
        uri: String,
        message: String,
        id: String,
        timeOfPost: String,
        senderId: String,
        groupId: String,
        commentId: String
    ) {
        this.uri = uri
        this.message = message
        this.id = id
        this.timeOfPost = timeOfPost
        this.senderId = senderId
        this.groupId = groupId
        this.commentId = commentId
    }


    fun getUri(): String?{
        return uri
    }

    fun setUri(uri: String){
        this.uri = uri
    }

    fun getMessage(): String?{
        return message
    }

    fun setMessage(message: String){
        this.message = message
    }
    fun getId(): String?{
        return id
    }

    fun setId(id: String){
        this.id = id
    }

    fun getTimeOfPost(): String?{
        return timeOfPost
    }

    fun setTimeOfPost(timeOfPost: String){
        this.timeOfPost = timeOfPost
    }

    fun getSenderId(): String?{
        return senderId
    }

    fun setSenderId(senderId: String){
        this.senderId = senderId
    }

    fun getGroupId(): String?{
        return groupId
    }

    fun setGroupId(groupId: String){
        this.groupId = groupId
    }
    fun getCommentId(): String?{
        return commentId
    }

    fun setCommentId(commentId: String){
        this.commentId = commentId
    }


}