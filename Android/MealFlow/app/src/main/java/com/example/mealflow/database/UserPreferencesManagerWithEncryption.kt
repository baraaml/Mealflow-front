//package com.example.mealflow.database
//
//import android.content.Context
//import androidx.security.crypto.EncryptedSharedPreferences
//import androidx.security.crypto.MasterKeys
//
//class UserPreferencesManagerWithEncryption(private val context: Context) {
//    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
//
//    private val sharedPreferences = EncryptedSharedPreferences.create(
//        "secure_prefs",                    // اسم الـ SharedPreferences
//        masterKeyAlias,                     // الـ Master Key
//        context,                            // السياق
//        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // تشفير المفتاح
//        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // تشفير القيم
//    )
//
//    companion object {
//        private const val USERNAME_KEY = "username"
//        private const val FIRST_NAME_KEY = "first_name"
//        private const val USER_ID_KEY = "user_id"
//        private const val MY_ID_KEY = "my_id"
//        private const val COMMUNITY_ID_KEY = "community_id"
//        private const val DARK_MODE_KEY = "dark_mode"
//    }
//
//    // حفظ البيانات
//    fun saveUsername(username: String) {
//        sharedPreferences.edit().putString(USERNAME_KEY, username).apply()
//    }
//
//    fun saveUserId(userId: String) {
//        sharedPreferences.edit().putString(USER_ID_KEY, userId).apply()
//    }
//
//    fun saveMyId(myId: String) {
//        sharedPreferences.edit().putString(MY_ID_KEY, myId).apply()
//    }
//
//    fun saveCommunityId(communityId: String) {
//        sharedPreferences.edit().putString(COMMUNITY_ID_KEY, communityId).apply()
//    }
//
//    fun saveFirstName(firstName: String) {
//        sharedPreferences.edit().putString(FIRST_NAME_KEY, firstName).apply()
//    }
//
//    fun saveDarkMode(isDarkMode: Boolean) {
//        sharedPreferences.edit().putBoolean(DARK_MODE_KEY, isDarkMode).apply()
//    }
//
//    // استرجاع البيانات
//    fun getUsername(): String {
//        return sharedPreferences.getString(USERNAME_KEY, "Unknown") ?: "Unknown"
//    }
//
//    fun getFirstName(): String {
//        return sharedPreferences.getString(FIRST_NAME_KEY, "Unknown") ?: "Unknown"
//    }
//
//    fun getUserId(): String {
//        return sharedPreferences.getString(USER_ID_KEY, "N/A") ?: "N/A"
//    }
//
//    fun getMyId(): String {
//        return sharedPreferences.getString(MY_ID_KEY, "N/A") ?: "N/A"
//    }
//
//    fun getCommunityId(): String {
//        return sharedPreferences.getString(COMMUNITY_ID_KEY, "N/A") ?: "N/A"
//    }
//
//    fun isDarkModeEnabled(): Boolean {
//        return sharedPreferences.getBoolean(DARK_MODE_KEY, false)
//    }
//
//    // حذف جميع البيانات
//    fun clearPreferences() {
//        sharedPreferences.edit().clear().apply()
//    }
//
//    // حذف بيانات معينة
//    fun clearUsername() {
//        sharedPreferences.edit().remove(USERNAME_KEY).apply()
//    }
//
//    fun clearFirstName() {
//        sharedPreferences.edit().remove(FIRST_NAME_KEY).apply()
//    }
//
//    fun clearUserId() {
//        sharedPreferences.edit().remove(USER_ID_KEY).apply()
//    }
//
//    fun clearMyId() {
//        sharedPreferences.edit().remove(MY_ID_KEY).apply()
//    }
//
//    fun clearCommunityId() {
//        sharedPreferences.edit().remove(COMMUNITY_ID_KEY).apply()
//    }
//
//    fun clearDarkMode() {
//        sharedPreferences.edit().remove(DARK_MODE_KEY).apply()
//    }
//}
