package com.example.mealflow.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SearchResponse(
    val success: Boolean,
    val message: String,
    val data: SearchData?
)

@Serializable
data class SearchData(
    val posts: List<Post>? = null,
    val users: List<SearchUser>? = null,
    val comments: List<SearchComment>? = null,
    val communities: List<SearchCommunity>? = null,
    val pagination: SearchPagination
)

@Serializable
data class SearchUser(
    val id: String,
    val name: String? = "null",
    val lastName: String? = "null",
    val username: String,
    val profilePicture: String?,
    val bio: String?,
    val createdAt: String,
    val isFollowing: Boolean,
    val _count: SearchUserCount
)

@Serializable
data class SearchUserCount(
    val posts: Int,
    val followers: Int,
    val following: Int
)

@Serializable
data class SearchComment(
    val id: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val parentId: String?,
    val author: CommentAuthor,
    val post: CommentPost,
    val likeCount: Int,
    val hasLiked : Boolean
)

@Serializable
data class CommentAuthor(
    val id: String,
    val name: String? = "null",
    val lastName: String? = "null",
    val username: String,
    val profilePicture: String?
)

@Serializable
data class CommentPost(
    val id: String,
    val title: String,
    val content: String,
    val author: CommentAuthor,
    val community: SearchCommunity? = null
)

//@Serializable
//data class SearchCommentCount(
//    val likes: Int
//)

@Serializable
data class SearchCommunity(
    val id: String,
    val name: String,
    val description: String,
    val image: String?,
    val privacy: String,
    val mealCreationPermission: String,
    val createdAt: String,
    val updatedAt: String,
    val owner: CommunityOwner,
    val categories: List<CommunityCategory>,
    val memberCount: Int,
    val postCount: Int,
    val _count: SearchCommunityCount
)

@Serializable
data class CommunityOwner(
    val id: String,
    val name: String,
    val lastName: String,
    val username: String,
    val profilePicture: String?
)

@Serializable
data class CommunityCategory(
    val id: String,
    val name: String
)

@Serializable
data class SearchCommunityCount(
    val members: Int,
    val posts: Int
)

@Serializable
data class SearchPagination(
    val totalCount: JsonElement? = null, // Can be Int or Object
    val hasMore: Boolean,
    val nextCursor: JsonElement? = null, // Can be String or Object
    val prevCursor: JsonElement? = null  // Can be String or Object
) {
    // Helper methods to extract values safely

    fun getTotalCountAsInt(): Int? {
        return when (totalCount) {
            is JsonPrimitive -> totalCount.int
            is JsonObject -> totalCount.jsonObject["total"]?.jsonPrimitive?.int
                ?: totalCount.jsonObject["count"]?.jsonPrimitive?.int
            else -> null
        }
    }

    fun getTotalCountObject(): SearchTotalCount? {
        return when (totalCount) {
            is JsonObject -> {
                val obj = totalCount.jsonObject
                SearchTotalCount(
                    posts = obj["posts"]?.jsonPrimitive?.int,
                    users = obj["users"]?.jsonPrimitive?.int,
                    comments = obj["comments"]?.jsonPrimitive?.int,
                    communities = obj["communities"]?.jsonPrimitive?.int,
                    total = obj["total"]?.jsonPrimitive?.int,
                    count = obj["count"]?.jsonPrimitive?.int
                )
            }
            is JsonPrimitive -> SearchTotalCount(count = totalCount.int)
            else -> null
        }
    }

    fun getNextCursorAsString(): String? {
        return when (nextCursor) {
            is JsonPrimitive -> nextCursor.jsonPrimitive.content
            is JsonObject -> {
                val obj = nextCursor.jsonObject
                obj["cursor"]?.jsonPrimitive?.content
                    ?: obj["posts"]?.jsonPrimitive?.content
                    ?: obj["users"]?.jsonPrimitive?.content
                    ?: obj["comments"]?.jsonPrimitive?.content
                    ?: obj["communities"]?.jsonPrimitive?.content
            }
            else -> null
        }
    }

    fun getNextCursorObject(): SearchCursors? {
        return when (nextCursor) {
            is JsonObject -> {
                val obj = nextCursor.jsonObject
                SearchCursors(
                    posts = obj["posts"]?.jsonPrimitive?.content,
                    users = obj["users"]?.jsonPrimitive?.content,
                    comments = obj["comments"]?.jsonPrimitive?.content,
                    communities = obj["communities"]?.jsonPrimitive?.content,
                    cursor = obj["cursor"]?.jsonPrimitive?.content
                )
            }
            is JsonPrimitive -> SearchCursors(cursor = nextCursor.jsonPrimitive.content)
            else -> null
        }
    }

    fun getPrevCursorAsString(): String? {
        return when (prevCursor) {
            is JsonPrimitive -> prevCursor.jsonPrimitive.content
            is JsonObject -> {
                val obj = prevCursor.jsonObject
                obj["cursor"]?.jsonPrimitive?.content
                    ?: obj["posts"]?.jsonPrimitive?.content
                    ?: obj["users"]?.jsonPrimitive?.content
                    ?: obj["comments"]?.jsonPrimitive?.content
                    ?: obj["communities"]?.jsonPrimitive?.content
            }
            else -> null
        }
    }

    fun getPrevCursorObject(): SearchCursors? {
        return when (prevCursor) {
            is JsonObject -> {
                val obj = prevCursor.jsonObject
                SearchCursors(
                    posts = obj["posts"]?.jsonPrimitive?.content,
                    users = obj["users"]?.jsonPrimitive?.content,
                    comments = obj["comments"]?.jsonPrimitive?.content,
                    communities = obj["communities"]?.jsonPrimitive?.content,
                    cursor = obj["cursor"]?.jsonPrimitive?.content
                )
            }
            is JsonPrimitive -> SearchCursors(cursor = prevCursor.jsonPrimitive.content)
            else -> null
        }
    }
}

// Keep these for backwards compatibility and type safety
@Serializable
data class SearchTotalCount(
    val posts: Int? = null,
    val users: Int? = null,
    val comments: Int? = null,
    val communities: Int? = null,
    val total: Int? = null,
    val count: Int? = null
)

@Serializable
data class SearchCursors(
    val posts: String? = null,
    val users: String? = null,
    val comments: String? = null,
    val communities: String? = null,
    val cursor: String? = null
)