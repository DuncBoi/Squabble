package com.duncboi.realsquabble

class Comments {

    private var user: String = ""
    private var comment: String = ""
    private var timePosted: String = ""

    constructor()
    constructor(user: String, comment: String, timePosted: String) {
        this.user = user
        this.comment = comment
        this.timePosted = timePosted
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



}