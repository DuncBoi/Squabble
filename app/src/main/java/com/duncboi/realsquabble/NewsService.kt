package com.duncboi.realsquabble

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface NewsService {
    @get: GET("v2/sources?language=en&country=us&category=general&apiKey=c6b76099eda8465aac148a69ad95b13c")
    val  sources: Call<WebSite>

    @GET fun getNewsFromSource(@Url url: String): Call<News>
}