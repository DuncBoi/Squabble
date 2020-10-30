package com.duncboi.realsquabble.ModelClasses

class Posts {

    private var type: String = ""
    private var description: String = ""
    private var votes: Int = 0
    private var creator: String = ""
    private var groupId: String = ""
    private var newsLink: String = ""
    private var mediaPic: String = ""
    private var timeOfPost: String = ""
    private var id: String  = ""
    private var zscore: Double = 0.0

    constructor()
    constructor(
        type: String,
        description: String,
        votes: Int,
        creator: String,
        groupId: String,
        newsLink: String,
        mediaPic: String,
        timeOfPost: String,
        id: String,
        zscore: Double
    ) {
        this.type = type
        this.description = description
        this.votes = votes
        this.creator = creator
        this.groupId = groupId
        this.newsLink = newsLink
        this.mediaPic = mediaPic
        this.timeOfPost = timeOfPost
        this.id = id
        this.zscore = zscore
    }


    fun getType(): String?{
        return type
    }

    fun setType(type: String){
        this.type = type
    }

    fun getDescription(): String?{
        return description
    }

    fun setDescription(description: String){
        this.description = description
    }

    fun getVotes(): Int?{
        return votes
    }

    fun setVotes(votes: Int){
        this.votes = votes
    }

    fun getCreator(): String?{
        return creator
    }

    fun setCreator(creator: String){
        this.creator = creator
    }

    fun getGroupId(): String?{
        return groupId
    }

    fun setGroupId(groupId: String){
        this.groupId = groupId
    }

    fun getNewsLink(): String?{
        return newsLink
    }

    fun setNewsLink(newsLink: String){
        this.newsLink = newsLink
    }

    fun getMediaPic(): String?{
        return mediaPic
    }

    fun setmMediaPic(mediaPic: String){
        this.mediaPic = mediaPic
    }
    fun getTimeOfPost(): String?{
        return timeOfPost
    }
    fun setTimeOfPost(timeOfPost: String){
        this.timeOfPost = timeOfPost
    }

    fun getId(): String?{
        return id
    }
    fun setId(id: String){
        this.id = id
    }

    fun getZscore(): Double?{
        return zscore
    }
    fun setZscore(zscore: Double){
        this.zscore = zscore
    }

}