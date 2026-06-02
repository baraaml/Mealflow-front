package com.example.mealflow.data.model

import kotlinx.serialization.Serializable

//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//---------------------------------- Community ---------------------------------------
@Serializable
data class CommunitiesResponse(
    val success: Boolean,
    val message: String,
    val data: CommunityData?
)

@Serializable
data class CommunityData(
    val communities: List<CommunityRole>,
    val pagination: Pagination,
)

@Serializable
data class CommunityRole(
    val role: String,
    val joinedAt: String,
    val isPending: Boolean,
    val community: Community
)

@Serializable
data class Community(
    val id: String,
    val name: String,
    val description: String,
    val image: String? = null,
    val ownerId: String? = null,
    val privacy: String,
    val _count: Count
)
@Serializable
data class Count(
    val members: Int,
)
//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//------------------------------ Single Community ------------------------------------
@Serializable
data class SingleCommunityResponse(
    val success: Boolean,
    val message: String,
    val community: SingleCommunity? = null
)

@Serializable
data class SingleCommunity(
    val id: String,
    val name: String,
    val description: String,
    val image: String,
    val privacy: String,
    val mealCreationPermission: String,
    val createdAt: String,
    val updatedAt: String,
    val categories: List<SingleCategory>,
    val members: List<SingleMember>,
    val owner: SingleUser,
    val _count: CountSingleCommunity,
    val hasMultipleAdmins : Boolean,
    val isMember: Boolean,
    val isAdmin: Boolean
)

@Serializable
data class SingleCategory(
    val id: String,
    val name: String,
    val parentId: String
)

@Serializable
data class SingleMember(
    val role: String,
    val joinedAt: String,
    val isPending: Boolean,
    val user: SingleUser
)

@Serializable
data class SingleUser(
    val id: String,
    val name: String?,
    val lastName: String?,
    val username: String,
    val profilePicture: String?
)

@Serializable
data class CountSingleCommunity(
    val members: Int,
    val posts: Int
)
//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//------------------------------------------------------------------------------------
//------------------------------ All Communities -------------------------------------
@Serializable
data class AllCommunitiesResponse(
    val success: Boolean,
    val count: Int,
    val communities: List<AllCommunities>
)

@Serializable
data class AllCommunities(
    val id: String,
    val name: String,
    val description: String,
    val image: String?,
    val privacy: String,
    val mealCreationPermission: String,
    val createdAt: String,
    val updatedAt: String,
    val categories: List<AllCommunitiesCategory>,
    val members: List<AllCommunitiesMember>,
    val owner: AllCommunitiesUser,
    val isMember: Boolean,
    val isAdmin: Boolean
)

@Serializable
data class AllCommunitiesCategory(
    val id: String,
    val name: String
)

@Serializable
data class AllCommunitiesMember(
    val role: String,
    val joinedAt: String,
    val user: AllCommunitiesUser
)

@Serializable
data class AllCommunitiesUser(
    val id: String,
    val name: String? = null,
    val username: String
)
