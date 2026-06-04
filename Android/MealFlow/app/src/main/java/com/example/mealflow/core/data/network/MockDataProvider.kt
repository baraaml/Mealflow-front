package com.example.mealflow.core.data.network

object MockDataProvider {

    fun getLoginResponse(): String {
        return """
            {
                "success": true,
                "message": "Login successful",
                "data": {
                    "accessToken": "fake-jwt-token",
                    "refreshToken": "fake-refresh-token",
                    "user": {
                        "id": "user123",
                        "username": "MockUser",
                        "name": "Mock User",
                        "profilePicture": "",
                        "email": "mock@example.com",
                        "isVerified": true
                    }
                }
            }
        """.trimIndent()
    }

    fun getRegisterResponse(): String {
        return """
            {
                "success": true,
                "message": "Registration successful",
                "data": {
                    "accessToken": "fake-jwt-token",
                    "refreshToken": "fake-refresh-token",
                    "user": {
                        "id": "user123",
                        "username": "MockUser",
                        "name": "Mock User",
                        "profilePicture": "",
                        "email": "mock@example.com",
                        "isVerified": false
                    }
                }
            }
        """.trimIndent()
    }

    fun getGenericSuccessResponse(message: String = "Operation successful"): String {
        return """
            {
                "success": true,
                "message": "$message",
                "data": null
            }
        """.trimIndent()
    }

    fun getMealListResponse(count: Int = 5): String {
        val mealsJson = (1..count).joinToString(",") { i ->
            """
            {
                "id": "meal_$i",
                "title": "Mock Meal $i",
                "description": "This is a delicious mock meal for testing.",
                "imageUrl": "https://picsum.photos/seed/meal$i/400/300",
                "region": "MockRegion",
                "calories": ${450.0 + i * 10},
                "totalTime": ${30 + i * 5},
                "isLiked": ${i % 2 == 0},
                "isFavorited": ${i % 3 == 0},
                "rating": ${4.0 + (i % 10) / 10.0},
                "createdBy": {
                    "user_id": "creator_$i",
                    "username": "Chef Mock $i"
                }
            }
            """
        }

        return """
            {
                "success": true,
                "count": $count,
                "meals": [$mealsJson]
            }
        """.trimIndent()
    }

    fun getCommunityListResponse(): String {
        val communitiesJson = (1..3).joinToString(",") { i ->
            """
            {
                "id": "$i",
                "name": "Mock Community $i",
                "description": "A group of mock people sharing mock recipes.",
                "memberCount": ${100 + i * 50},
                "isJoined": ${i == 1}
            }
            """
        }
        return """
            {
                "success": true,
                "communities": [$communitiesJson]
            }
        """.trimIndent()
    }

    fun getPostListResponse(): String {
        val postsJson = (1..5).joinToString(",") { i ->
            """
            {
                "id": "post_$i",
                "content": "Check out this amazing meal I just planned! #MockFlow",
                "author": {
                    "username": "User_$i"
                },
                "likes": ${10 * i},
                "createdAt": "2026-06-02T21:20:52Z"
            }
            """
        }
        return """
            {
                "success": true,
                "posts": [$postsJson]
            }
        """.trimIndent()
    }
}
