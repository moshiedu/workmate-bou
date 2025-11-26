package com.moshitech.workmate.feature.deviceinfo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moshitech.workmate.feature.deviceinfo.data.models.TestStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "test_results")

class TestResultsRepository(private val context: Context) {

    fun getTestStatus(testId: String): Flow<TestStatus> {
        val key = stringPreferencesKey("status_$testId")
        return context.dataStore.data.map { preferences ->
            val statusName = preferences[key] ?: TestStatus.UNTESTED.name
            try {
                TestStatus.valueOf(statusName)
            } catch (e: IllegalArgumentException) {
                TestStatus.UNTESTED
            }
        }
    }

    suspend fun setTestStatus(testId: String, status: TestStatus) {
        val key = stringPreferencesKey("status_$testId")
        context.dataStore.edit { preferences ->
            preferences[key] = status.name
        }
    }

    suspend fun resetAllTests(testIds: List<String>) {
        context.dataStore.edit { preferences ->
            testIds.forEach { id ->
                val key = stringPreferencesKey("status_$id")
                preferences.remove(key)
            }
        }
    }
}
