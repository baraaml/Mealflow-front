package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostsResponse(
    val success: Boolean,
    val message: String,
    val length: Int?,
    val data: PostsData?
)
@Serializable
data class PostsData(
    val posts: List<Post>,
    val pagination: Pagination
)
@Serializable
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val targetType: String,
    val mediaType: String,
    val mediaUrl: String?,
    val isPinned: Boolean,
    val isEdited: Boolean,
    val lastEditedAt: String?,
    val isHidden: Boolean,
    val hiddenReason: String?,
    val shareCount: Int,
    val authorId: String,
    val communityId: String?,
    val flairId: String?,
    val viewCount: Int,
    val isLocked: Boolean,
    val allowComments: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val author: Author,
    val flair: Flair?,
    val community: GetCommunityPost? = null, // Can be null or a Community object
    val likeCount: Int,
    val commentCount: Int,
    val hasLiked: Boolean
)
@Serializable
data class Author(
    val id: String,
    val name: String?,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val username: String,
)
@Serializable
data class Flair(
    val id: String,
    val name: String
)
@Serializable
data class Pagination(
    val nextCursor: String? = null,  // Make nullable with default value
    val prevCursor: String? = null,  // Make nullable with default value
    val hasMore: Boolean
)

@Serializable
data class GetCommunityPost(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val image: String? = null
)