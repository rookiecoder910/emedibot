package com.example.emedibotsimpleuserlogin



import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("intro_prefs")

class IntroPreferences(private val context: Context) {
    companion object {
        private val HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro")
    }

    val hasSeenIntro: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[HAS_SEEN_INTRO] ?: false }

    suspend fun setHasSeenIntro(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[HAS_SEEN_INTRO] = value }
    }
}
