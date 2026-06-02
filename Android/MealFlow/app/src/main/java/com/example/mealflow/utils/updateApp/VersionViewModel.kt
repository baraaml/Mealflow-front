package com.example.mealflow.utils.updateApp

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VersionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VersionRepository(application.applicationContext)
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _versionState = MutableStateFlow<VersionState>(VersionState.Idle)
    val versionState: StateFlow<VersionState> = _versionState

    sealed class VersionState {
        object Idle : VersionState()
        object Loading : VersionState()
        data class Success(
            val versionData: VersionData,
            val currentAppVersion: String,
            val isLatestVersion: Boolean,
            val needsUpdate: Boolean,
            val versionComparison: VersionComparison
        ) : VersionState()
        data class Error(val message: String) : VersionState()
    }

    enum class VersionComparison {
        UP_TO_DATE,        // The current version is the latest
        UPDATE_AVAILABLE,  // There is a newer version.
        FORCE_UPDATE,      // Forced update
        UNSUPPORTED        // Current version is not supported
    }

    fun checkAppVersion() {
        viewModelScope.launch {
            _versionState.value = VersionState.Loading

            // Get the current version of the app
            val currentVersion = getCurrentAppVersion()

            val result = repository.checkVersion()
            result.onSuccess { versionData ->
                val comparison = compareVersions(currentVersion, versionData)
                val isLatest = comparison == VersionComparison.UP_TO_DATE
                val needsUpdate = comparison != VersionComparison.UP_TO_DATE

                _versionState.value = VersionState.Success(
                    versionData = versionData,
                    currentAppVersion = currentVersion,
                    isLatestVersion = isLatest,
                    needsUpdate = needsUpdate,
                    versionComparison = comparison
                )
            }.onFailure { exception ->
                _versionState.value = VersionState.Error(exception.message ?: "Unknown Error")
            }
        }
    }

    fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    private fun compareVersions(currentVersion: String, apiVersionData: VersionData): VersionComparison {
        val current = parseVersion(currentVersion)
        val latest = parseVersion(apiVersionData.latestVersion)
        val minSupported = parseVersion(apiVersionData.minSupportedVersion)

        // Check that the current version is supported
        if (isVersionLowerThan(current, minSupported)) {
            return VersionComparison.UNSUPPORTED
        }

        // Check for forced update
        if (apiVersionData.forceUpdate && isVersionLowerThan(current, latest)) {
            return VersionComparison.FORCE_UPDATE
        }

        // Compare the current version with the latest
        return when {
            isVersionLowerThan(current, latest) -> VersionComparison.UPDATE_AVAILABLE
            else -> VersionComparison.UP_TO_DATE
        }
    }

    private fun parseVersion(version: String): List<Int> {
        return version.split(".")
            .mapNotNull { it.toIntOrNull() }
            .takeIf { it.isNotEmpty() } ?: listOf(0)
    }

    private fun isVersionLowerThan(version1: List<Int>, version2: List<Int>): Boolean {
        val maxLength = maxOf(version1.size, version2.size)

        for (i in 0 until maxLength) {
            val v1 = version1.getOrElse(i) { 0 }
            val v2 = version2.getOrElse(i) { 0 }

            when {
                v1 < v2 -> return true
                v1 > v2 -> return false
            }
        }
        return false // The copies are identical
    }
}