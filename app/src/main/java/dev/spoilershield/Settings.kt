package dev.spoilershield
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
val Context.settings by preferencesDataStore("shield")
object Preferences {
 val consent = booleanPreferencesKey("consent")
 val mode = stringPreferencesKey("mode")
 val blockedApps = stringSetPreferencesKey("blocked_apps")
 val youtube = booleanPreferencesKey("youtube_consent")
 val enabled = booleanPreferencesKey("enabled")
}
object Health { val status = kotlinx.coroutines.flow.MutableStateFlow("Accessibility service disconnected") }
