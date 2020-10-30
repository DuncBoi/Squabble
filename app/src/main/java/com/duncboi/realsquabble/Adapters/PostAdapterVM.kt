package com.duncboi.realsquabble.Adapters

import androidx.recyclerview.widget.RecyclerView
import com.duncboi.realsquabble.ModelClasses.Posts


object PostAdapterVM {

    var groupOrderedPosts: List<Posts>? = ArrayList()
    var groupmResults: Int? = null
    var groupPaginatorPosts: List<Posts> = ArrayList()
    var groupPostAdapter: PostAdapter? = null
    var groupRecyclerView: RecyclerView? = null
    var groupPosition = 0

    var trendingOrderedPosts: List<Posts>? = ArrayList()
    var trendingmResults: Int? = null
    var trendingPaginatorPosts: List<Posts>? = ArrayList()
    var trendingPostAdapter: PostAdapter? = null
    var trendingRecyclerView: RecyclerView? = null
    var trendingPosition = 0
    var trendingPag: List<Posts>? = ArrayList()

    var partyOrderedPosts: List<Posts>? = ArrayList()
    var partymResults: Int? = null
    var partyPaginatorPosts: List<Posts>? = ArrayList()
    var partyPostAdapter: PostAdapter? = null
    var partyRecyclerView: RecyclerView? = null
    var partyPosition = 0
}