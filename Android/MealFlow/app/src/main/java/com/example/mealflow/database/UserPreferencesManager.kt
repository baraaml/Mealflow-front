package com.example.mealflow.database

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")
private val Context.communityDataStore by preferencesDataStore(name = "community_prefs")

class UserPreferencesManager(private val context: Context) {
    companion object {
        private val USERNAME = stringPreferencesKey("username")
        private val USER_ID = stringPreferencesKey("user_id")
        private val MY_ID = stringPreferencesKey("my_id")
        private val MY_IMAGE_PROFILE = stringPreferencesKey("my_image_profile")
        private val COMMUNITY_ID = stringPreferencesKey("community_id")
        private val FIRST_NAME = stringPreferencesKey("first_name")
        private val THEME_SETTING = stringPreferencesKey("theme_setting")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        
        // Community preference keys
        private val IS_MEMBER = booleanPreferencesKey("is_member")
        private val MEMBER_ROLE = stringPreferencesKey("member_role")
    }

    // Username
    suspend fun setUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    fun getUsername(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME] ?: "Unknown"
        }
    }

    // User ID
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }

    fun getUserId(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID] ?: "N/A"
        }
    }

    suspend fun getUserIdString(): String {
        return context.dataStore.data
            .map { preferences -> preferences[USER_ID] ?: "N/A" }
            .first()
    }

    // My ID
    suspend fun saveMyId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[MY_ID] = userId
        }
    }

    suspend fun getMyId(): String {
        return context.dataStore.data
            .map { preferences -> preferences[MY_ID] ?: "N/A" }
            .first()
    }

    // My Image Profile
    suspend fun saveMyImageProfile(urlImage: String) {
        context.dataStore.edit { preferences ->
            preferences[MY_IMAGE_PROFILE] = urlImage
        }
    }

    suspend fun getMyImageProfile(): String {
        return context.dataStore.data
            .map { preferences -> preferences[MY_IMAGE_PROFILE] ?: "N/A" }
            .first()
    }

    // Community ID
    suspend fun saveCommunityId(communityId: String) {
        context.dataStore.edit { preferences ->
            preferences[COMMUNITY_ID] = communityId
        }
    }

    fun getCommunityId(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[COMMUNITY_ID] ?: "N/A"
        }
    }

    // First Name
    suspend fun saveFirstName(firstName: String) {
        context.dataStore.edit { preferences ->
            preferences[FIRST_NAME] = firstName
        }
    }

    suspend fun getFirstname(): String {
        return context.dataStore.data
            .map { preferences -> preferences[FIRST_NAME] ?: "Unknown" }
            .first()
    }

    // Community Member Status
    suspend fun setCommunityMember(communityId: String, isMember: Boolean, role: String? = null) {
        context.communityDataStore.edit { preferences ->
            preferences[IS_MEMBER] = isMember
            role?.let { preferences[MEMBER_ROLE] = it }
        }
    }

    fun getCommunityMemberStatus(): Flow<Boolean> {
        return context.communityDataStore.data.map { preferences ->
            preferences[IS_MEMBER] ?: false
        }
    }

    fun getCommunityMemberRole(): Flow<String?> {
        return context.communityDataStore.data.map { preferences ->
            preferences[MEMBER_ROLE]
        }
    }

    // Theme Setting
    suspend fun setThemeSetting(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_SETTING] = theme
        }
    }

    fun getThemeSetting(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_SETTING] ?: "System"
        }
    }

    // Notifications
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    fun getNotificationsEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }
    }

    // Haptic Feedback
    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    fun getHapticFeedbackEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[HAPTIC_FEEDBACK_ENABLED] ?: true
        }
    }

    // Clear all preferences
    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        context.communityDataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 