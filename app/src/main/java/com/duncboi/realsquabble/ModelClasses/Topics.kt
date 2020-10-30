package com.duncboi.realsquabble.ModelClasses

class Topics {

    private var id: String = ""
    private var headline: String = ""
    private var description: String = ""
    private var uri: String = ""
    private var istrending: Boolean = false
    private var creator: String = ""
    private var question: String = ""
    private var answer1: String = ""
    private var answer2: String = ""
    private var includeUserInfo: Boolean = true

    constructor()
    constructor(
        id: String,
        headline: String,
        description: String,
        uri: String,
        istrending: Boolean,
        creator: String,
        question: String,
        answer1: String,
        answer2: String,
        includeUserInfo: Boolean
    ) {
        this.id = id
        this.headline = headline
        this.description = description
        this.uri = uri
        this.istrending = istrending
        this.creator = creator
        this.question = question
        this.answer1 = answer1
        this.answer2 = answer2
        this.includeUserInfo = includeUserInfo
    }


    fun getHeadline(): String?{
        return headline
    }

    fun setHeadline(headline: String){
        this.headline = headline
    }
    fun getDescription(): String?{
        return description
    }

    fun setDescription(description: String){
        this.description = description
    }
    fun getUri(): String?{
        return uri
    }

    fun setUri(uri: String){
        this.uri = uri
    }

    fun getIsTrending(): Boolean?{
        return istrending
    }

    fun setIsTrending(istrending: Boolean){
        this.istrending = istrending
    }

    fun getCreator(): String?{
        return creator
    }

    fun setCreator(creator: String){
        this.creator = creator
    }
    fun getQuestion(): String?{
        return question
    }

    fun setQuestion(question: String){
        this.question = question
    }
    fun getAnswer1(): String?{
        return answer1
    }

    fun setAnswer1(answer1: String){
        this.answer1 = answer1
    }

    fun getAnswer2(): String?{
        return answer2
    }

    fun setAnswer2(answer2: String){
        this.answer2 = answer2
    }

    fun getIncludeUserInfo(): Boolean?{
        return includeUserInfo
    }

    fun setIncludeUserInfo(includeUserInfo: Boolean){
        this.includeUserInfo = includeUserInfo
    }

    fun getId(): String?{
        return id
    }

    fun setId(id: String){
        this.id = id
    }


}