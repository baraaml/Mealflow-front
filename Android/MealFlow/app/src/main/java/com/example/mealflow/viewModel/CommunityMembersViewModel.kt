package com.example.mealflow.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.mealflow.network.CommunityMember
import com.example.mealflow.network.CommunityMembersApiService
import com.example.mealflow.network.UserMember
import com.example.mealflow.utils.CommunityMembersPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

//@OptIn(ExperimentalCoroutinesApi::class)
//class CommunityMembersViewModel(application: Application) : AndroidViewModel(application) {
//
//    companion object {
//        private const val TAG = "CommunityMembersViewModel"
//        private const val PAGE_SIZE = 5
//        private const val INITIAL_LOAD_SIZE = 10
//    }
//
//    private val communityMembersApiService = CommunityMembersApiService(application.applicationContext)
//
//    private val refreshTrigger = MutableStateFlow(0)
//
//    private val _errorState = MutableStateFlow<String?>(null)
//    val errorState = _errorState.asStateFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading = _isLoading.asStateFlow()
//
//    val communityMembersFlow: Flow<PagingData<CommunityMember>> = refreshTrigger.flatMapLatest {
//        Pager(
//            config = PagingConfig(
//                pageSize = PAGE_SIZE,
//                prefetchDistance = 2,
//                initialLoadSize = INITIAL_LOAD_SIZE,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = {
//                CommunityMembersPagingSource(communityMembersApiService)
//            }
//        ).flow
//    }
//        .cachedIn(viewModelScope)
//        .distinctUntilChanged()
//
//    fun refresh() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                refreshTrigger.value += 1
//                _errorState.value = null
//                Log.d(TAG, "Refreshing community members data")
//            } catch (e: Exception) {
//                _errorState.value = e.message ?: "An unknown error occurred while updating the community members list."
//                Log.e(TAG, "Error refreshing community members", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun clearError() {
//        _errorState.value = null
//    }
//}
@OptIn(ExperimentalCoroutinesApi::class)
class CommunityMembersViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "CommunityMembersViewModel"
        private const val PAGE_SIZE = 5
        private const val INITIAL_LOAD_SIZE = 10
    }

    private val communityMembersApiService = CommunityMembersApiService(application.applicationContext)

    private val refreshTrigger = MutableStateFlow(0)

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // إضافة owner state
    private val _owner = MutableStateFlow<UserMember?>(null)
    val owner = _owner.asStateFlow()

    val communityMembersFlow: Flow<PagingData<CommunityMember>> = refreshTrigger.flatMapLatest {
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 2,
                initialLoadSize = INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                CommunityMembersPagingSource(communityMembersApiService) { ownerData ->
                    // تحديث الـ owner عند تحميل البيانات
                    _owner.value = ownerData
                }
            }
        ).flow
    }
        .cachedIn(viewModelScope)
        .distinctUntilChanged()

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                refreshTrigger.value += 1
                _errorState.value = null
                Log.d(TAG, "Refreshing community members data")
            } catch (e: Exception) {
                _errorState.value = e.message ?: "An unknown error occurred while updating the community members list."
                Log.e(TAG, "Error refreshing community members", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorState.value = null
    }
}