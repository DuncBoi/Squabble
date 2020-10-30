package com.duncboi.realsquabble.ModelClasses

class Groups {
    private var chatid: String = ""
    private var creator: String = ""
    private var description: String = ""
    private var name: String = ""
    private var type: String = ""
    private var uri: String = ""
    private var stateLocation: String = ""
    private var countyLocation: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var distance: Double = 0.0

    constructor()
    constructor(
        chatid: String,
        creator: String,
        description: String,
        name: String,
        type: String,
        uri: String,
        stateLocation: String,
        countyLocation: String,
        latitude: String,
        longitude: String,
        distance: Double
    ) {
        this.chatid = chatid
        this.creator = creator
        this.description = description
        this.name = name
        this.type = type
        this.uri = uri
        this.stateLocation = stateLocation
        this.countyLocation = countyLocation
        this.latitude = latitude
        this.longitude = longitude
        this.distance = distance
    }


    fun getChatId(): String?{
        return chatid
    }

    fun setChatId(chatid: String){
        this.chatid = chatid
    }
    fun getCreator(): String?{
        return creator
    }

    fun setCreator(creator: String){
        this.creator = creator
    }
    fun getDescription(): String?{
        return description
    }

    fun setDescription(description: String){
        this.description = description
    }
    fun getName(): String?{
        return name
    }

    fun setName(name: String){
        this.name = name
    }
    fun getType(): String?{
        return type
    }

    fun setType(type: String){
        this.type = type
    }
    fun getUri(): String?{
        return uri
    }

    fun setUri(uri: String){
        this.uri = uri
    }

    fun getStateLocation(): String?{
        return stateLocation
    }

    fun setStateLocation(stateLocation: String){
        this.stateLocation = stateLocation
    }

    fun getCountyLocation(): String?{
        return countyLocation
    }

    fun setCountyLocation(countyLocation: String){
        this.countyLocation = countyLocation
    }

    fun getLongitude(): String?{
        return longitude
    }

    fun setLongitude(longitude: String){
        this.longitude = longitude
    }

    fun getLatitude(): String?{
        return latitude
    }

    fun setLatitude(latitude: String){
        this.latitude = latitude
    }

    fun getDistance(): Double?{
        return distance
    }

    fun setDistance(distance: Double){
        this.distance = distance
    }
}