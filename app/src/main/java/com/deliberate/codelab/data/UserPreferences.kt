package com.deliberate.codelab.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This extension property ensures we only have one instance of DataStore in the app
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    // Define the keys we want to save
    companion object {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    // 1. Write the data
    suspend fun completeOnboarding(language: String) {
        Log.d("DataStoreDebug", "1. Action Triggered: Attempting to save to disk...")
        try {
            context.dataStore.edit { prefs ->
                prefs[HAS_SEEN_ONBOARDING] = true
                prefs[SELECTED_LANGUAGE] = language
            }
            Log.d("DataStoreDebug", "2. SUCCESS: Data physically written to disk!")
        } catch (e: Exception) {
            Log.e("DataStoreDebug", "ERROR: Failed to write to disk!", e)
        }
    }

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val hasSeen = prefs[HAS_SEEN_ONBOARDING] ?: false
        Log.d("DataStoreDebug", "3. Flow Read: Emitting value -> $hasSeen")
        hasSeen
    }
}