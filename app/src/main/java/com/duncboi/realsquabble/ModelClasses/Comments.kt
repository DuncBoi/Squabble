package com.duncboi.realsquabble.ModelClasses

class Comments {

    private var user: String = ""
    private var comment: String = ""
    private var timePosted: String = ""
    private var commentId: String = ""
    private var likeNumber: Long = 0

    constructor()
    constructor(
        user: String,
        comment: String,
        timePosted: String,
        commentId: String,
        likeNumber: Long
    ) {
        this.user = user
        this.comment = comment
        this.timePosted = timePosted
        this.commentId = commentId
        this.likeNumber = likeNumber
    }


    fun getUser (): String?{
        return this.user
    }

    fun setUser (user:String){
        this.user = user
    }
    fun getComment (): String?{
        return this.comment
    }

    fun setComment (comment:String){
        this.comment = comment
    }

    fun getTimePosted (): String?{
        return this.timePosted
    }

    fun setTimePosted (timePosted:String){
        this.timePosted = timePosted
    }

    fun getCommentId (): String?{
        return this.commentId
    }

    fun setCommentId (commentId:String){
        this.commentId = commentId
    }

    fun getLikeNumber (): Long?{
        return this.likeNumber
    }

    fun setLikeNumber (likeNumber:Long){
        this.likeNumber = likeNumber
    }



}