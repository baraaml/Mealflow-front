//package com.example.mealflow.database
//
//import android.content.Context
//import com.example.mealflow.database.dao.PostDao
//import com.example.mealflow.database.dao.UserDao
//import com.example.mealflow.database.entity.UserProfile
//import com.example.mealflow.network.MyProfileApiService
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//class ProfileRepository(private val userDao: UserDao, private val postDao: PostDao) {
//    val userProfile = userDao.getUserProfileLiveData()
//    val userPosts = postDao.getUserPostsLiveData("")
//
//    suspend fun refreshProfile(context: Context) {
//        withContext(Dispatchers.IO) {
//            val apiService = MyProfileApiService(context)
//            val result = apiService.fetchUserProfile()
//
//            if (result.success) {
//                val profile = UserProfile(
//                    id = result.data.id,
//                    name = result.data.name,
//                    username = result.data.username,
//                    email = result.data.email,
//                    isVerified = result.data.isVerified,
//                    verifiedAt = result.data.verifiedAt,
//                    createdAt = result.data.createdAt,
//                    updatedAt = result.data.updatedAt,
////                    communitiesOwned = result.data.communitiesOwned,
////                    communityMemberships = result.data.communityMemberships
//                )
//
//                userDao.insertUserProfile(profile)
//                // Save posts and other data as needed
//            }
//        }
//    }
//}