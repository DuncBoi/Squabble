package com.duncboi.realsquabble.ModelClasses

class Users {

    private var category: String = ""
    private var economicScore: String = ""
    private var socialScore: String = ""
    private var anonymous: String = ""
    private var email: String = ""
    private var name: String = ""
    private var uid: String = ""
    private var username = ""
    private var bio: String = ""
    private var uri: String = ""
    private var password: String = ""
    private var status: String = ""
    private var userTime: String = ""
    private var groupId: String = ""
    private var alignmentTime: String = ""
    private var unread: Boolean = true
    private var timeOfMessage: Long = 0

    constructor()
    constructor(
        category: String,
        economicScore: String,
        socialScore: String,
        anonymous: String,
        email: String,
        name: String,
        uid: String,
        username: String,
        bio: String,
        uri: String,
        password: String,
        status: String,
        userTime: String,
        groupId: String,
        alignmentTime: String,
        unread: Boolean,
        timeOfMessage: Long
    ) {
        this.category = category
        this.economicScore = economicScore
        this.socialScore = socialScore
        this.anonymous = anonymous
        this.email = email
        this.name = name
        this.uid = uid
        this.username = username
        this.bio = bio
        this.uri = uri
        this.password = password
        this.status = status
        this.userTime = userTime
        this.groupId = groupId
        this.alignmentTime = alignmentTime
        this.unread = unread
        this.timeOfMessage = timeOfMessage
    }


    fun getCategory(): String?{
        return category
    }

    fun setCategory(category: String){
        this.category = category
    }

    fun getEconomicScore(): String?{
        return economicScore
    }

    fun setEconomicScore(economicScore: String){
        this.economicScore = economicScore
    }

    fun getSocialScore(): String?{
        return socialScore
    }

    fun setSocialScore(socialScore: String){
        this.socialScore = socialScore
    }

    fun getAnonymous(): String?{
        return anonymous
    }

    fun setAnonymous(anonymous: String){
        this.anonymous = anonymous
    }

    fun getEmail(): String?{
        return email
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun getName(): String?{
        return name
    }

    fun setName(name: String){
        this.name = name
    }

    fun getUid(): String?{
        return uid
    }

    fun setUid(uid: String){
        this.uid = uid
    }

    fun getUsername(): String?{
        return username
    }

    fun setUsername(username: String){
        this.username = username
    }

    fun getBio(): String?{
        return bio
    }

    fun setBio(bio: String){
        this.bio = bio
    }

    fun getUri(): String?{
        return uri
    }

    fun setUri(uri: String){
        this.uri = uri
    }
    fun getPassword(): String?{
        return password
    }

    fun setPassword(password: String){
        this.password = password
    }

    fun getStatus(): String?{
        return status
    }

    fun setStatus(status: String){
        this.status = status
    }

    fun getUserTime(): String?{
        return userTime
    }

    fun setUserTime(userTime: String){
        this.userTime = userTime
    }

    fun getGroupId(): String?{
        return groupId
    }

    fun setGroupId(groupId: String){
        this.groupId = groupId
    }

    fun getAlignmentTime(): String?{
        return alignmentTime
    }

    fun setAlignmentTime(alignmentTime: String){
        this.alignmentTime = alignmentTime
    }

    fun getUnread(): Boolean?{
        return unread
    }

    fun setUnread(unread: Boolean){
        this.unread = unread
    }

    fun getTimeOfMessage(): Long?{
        return timeOfMessage
    }

    fun setTimeOfMessage(timeOfMessage: Long){
        this.timeOfMessage = timeOfMessage
    }


}