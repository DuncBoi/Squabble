package com.duncboi.realsquabble

class Groups {
    private var chatid: String = ""
    private var creator: String = ""
    private var description: String = ""
    private var name: String = ""
    private var type: String = ""
    private var uri: String = ""

    constructor()
    constructor(
        chatid: String,
        creator: String,
        description: String,
        name: String,
        type: String,
        uri: String
    ) {
        this.chatid = chatid
        this.creator = creator
        this.description = description
        this.name = name
        this.type = type
        this.uri = uri
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

}