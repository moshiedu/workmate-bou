package com.moshitech.workmate.feature.imagestudio.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MonetizationManager {
    
    // TODO: Implement comprehensive monetization plan across all modules
    // For now, all features are unlocked
    private val _isPro = MutableStateFlow(true)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    // Feature Limits //
    const val FREE_BATCH_LIMIT = 5
    
    // Feature Checks //
    fun isBatchLimitReached(count: Int): Boolean {
        if (_isPro.value) return false
        return count >= FREE_BATCH_LIMIT
    }
    
    fun isFilterLocked(filterId: String): Boolean {
        if (_isPro.value) return false
        return filterId.startsWith("pro_")
    }

    fun isHighQualitySaveLocked(): Boolean {
        return !_isPro.value
    }
    
    // Debug/Testing //
    fun setProStatus(isPro: Boolean) {
        _isPro.value = isPro
    }
}
