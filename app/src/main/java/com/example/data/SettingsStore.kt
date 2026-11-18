package com.example.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ThemeMode
import com.example.model.UserSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dev_social_settings")

class SettingsStore(private val context: Context) {

    private companion object {
        const val TAG = "SettingsStore"
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIGH_GLASS_INTENSITY = booleanPreferencesKey("high_glass_intensity")
        val PRIVATE_ACCOUNT = booleanPreferencesKey("private_account")
        val SHOW_ONLINE_STATUS = booleanPreferencesKey("show_online_status")
        val NOTIFY_LIKES = booleanPreferencesKey("notify_likes")
        val NOTIFY_COMMENTS = booleanPreferencesKey("notify_comments")
        val NOTIFY_REQUESTS = booleanPreferencesKey("notify_requests")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val BIO = stringPreferencesKey("bio")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is CancellationException) throw exception
            Log.e(TAG, "Failed to read DataStore preferences; falling back to defaults", exception)
            emit(emptyPreferences())
        }
        .map { prefs ->
            val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
            UserSettings(
                themeMode = themeMode,
                highGlassIntensity = prefs[Keys.HIGH_GLASS_INTENSITY] ?: true,
                privateAccount = prefs[Keys.PRIVATE_ACCOUNT] ?: false,
                showOnlineStatus = prefs[Keys.SHOW_ONLINE_STATUS] ?: true,
                notifyLikes = prefs[Keys.NOTIFY_LIKES] ?: true,
                notifyComments = prefs[Keys.NOTIFY_COMMENTS] ?: true,
                notifyRequests = prefs[Keys.NOTIFY_REQUESTS] ?: true,
                displayName = prefs[Keys.DISPLAY_NAME] ?: "Alex Rivera",
                bio = prefs[Keys.BIO]
                    ?: "Mobile UI Architect crafting Liquid Glass interfaces, Kotlin Multiplatform tools, and spatial motion."
            )
        }

    suspend fun setThemeMode(mode: ThemeMode): Result<Unit> = safeEdit {
        it[Keys.THEME_MODE] = mode.name
    }

    suspend fun setHighGlassIntensity(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.HIGH_GLASS_INTENSITY] = enabled
    }

    suspend fun setPrivateAccount(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.PRIVATE_ACCOUNT] = enabled
    }

    suspend fun setShowOnlineStatus(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.SHOW_ONLINE_STATUS] = enabled
    }

    suspend fun setNotifyLikes(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.NOTIFY_LIKES] = enabled
    }

    suspend fun setNotifyComments(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.NOTIFY_COMMENTS] = enabled
    }

    suspend fun setNotifyRequests(enabled: Boolean): Result<Unit> = safeEdit {
        it[Keys.NOTIFY_REQUESTS] = enabled
    }

    suspend fun updateProfile(displayName: String, bio: String): Result<Unit> = safeEdit {
        it[Keys.DISPLAY_NAME] = displayName.trim().ifEmpty { "Alex Rivera" }
        it[Keys.BIO] = bio.trim()
    }

    suspend fun resetAll(): Result<Unit> = safeEdit {
        it.clear()
    }

    private suspend fun safeEdit(
        transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit
    ): Result<Unit> {
        return try {
            context.dataStore.edit(transform)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist preference change", e)
            Result.failure(e)
        }
    }
}
