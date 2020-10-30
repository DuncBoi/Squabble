package com.duncboi.realsquabble.ModelClasses

class Queue {

    private var host: Boolean = false
    private var uid: String = ""
    private var timeOfQueue: Long = 0
    private var randomNumber: String = ""
    private var randomNumber2: String = ""
    private var randomNumber3: String = ""


    constructor()
    constructor(host: Boolean, uid: String, timeOfQueue: Long) {
        this.host = host
        this.uid = uid
        this.timeOfQueue = timeOfQueue
    }

    fun getHost(): Boolean?{
        return host
    }
    fun setHost(host: Boolean){
        this.host = host
    }
    fun getUid(): String?{
        return uid
    }
    fun setUid(uid: String){
        this.uid = uid
    }
    fun getTimeOfQueue(): Long?{
        return timeOfQueue
    }
    fun setTimeOfQueue(timeOfQueue: Long){
        this.timeOfQueue = timeOfQueue
    }
    fun getRandomNumber(): String?{
        return randomNumber
    }
    fun setRandomNumber(randomNumber: String){
        this.randomNumber = randomNumber
    }
    fun getRandomNumber2(): String?{
        return randomNumber2
    }
    fun setRandomNumber2(randomNumber2: String){
        this.randomNumber2 = randomNumber2
    }
    fun getRandomNumber3(): String?{
        return randomNumber3
    }
    fun setRandomNumber3(randomNumber3: String){
        this.randomNumber3 = randomNumber3
    }


}