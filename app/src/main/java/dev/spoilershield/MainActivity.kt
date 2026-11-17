package dev.spoilershield

import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
 private var accessibilityEnabled by mutableStateOf(false)
 private var installedApps by mutableStateOf<List<InstalledApps.App>>(emptyList())
 override fun onResume() {
  super.onResume()
  val manager = getSystemService(android.view.accessibility.AccessibilityManager::class.java)
  accessibilityEnabled = manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
   it.resolveInfo.serviceInfo.packageName == packageName && it.resolveInfo.serviceInfo.name == ShieldService::class.java.name
  }
  lifecycleScope.launch { installedApps = withContext(Dispatchers.IO) { InstalledApps.load(this@MainActivity) } }
 }
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   val prefs by settings.data.collectAsState(emptyPreferences())
   val health by Health.status.collectAsState()
   val mode = ProtectionMode.fromStored(prefs[Preferences.mode])
   val selected = prefs[Preferences.blockedApps].orEmpty()
   val availableSelected = selected.intersect(installedApps.map { it.packageName }.toSet())
   var showAllApps by remember { mutableStateOf(false) }
   var search by remember { mutableStateOf("") }
   MaterialTheme(colorScheme = darkColorScheme()) {
    Surface(Modifier.fillMaxSize()) {
     Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Spacer(Modifier.height(16.dp))
      Text("Spoiler Shield", style = MaterialTheme.typography.headlineLarge)
      Text("Choose how you stay spoiler-free.", style = MaterialTheme.typography.titleMedium)
      Text("Text filters inspect accessible Instagram captions and descriptions locally. YouTube inspection requires a separate opt-in. App blocking checks only which selected app is open. No screen text is stored or sent anywhere.")
      if (prefs[Preferences.consent] != true) {
       Text("Accessibility access lets Shield inspect supported views and draw covers. You can revoke access or turn protection off at any time.")
       Button(onClick = { lifecycleScope.launch { settings.edit { it[Preferences.consent] = true } } }) { Text("I agree to enable Shield") }
      } else {
       Text("Protection mode", style = MaterialTheme.typography.titleLarge)
       ProtectionMode.entries.forEach { option ->
        OutlinedCard(onClick = { lifecycleScope.launch {
         settings.edit { values ->
          values[Preferences.mode] = option.name
          if (option == ProtectionMode.APPS && availableSelected.isEmpty()) values[Preferences.enabled] = false
         }
        } }, modifier = Modifier.fillMaxWidth()) {
         Row(Modifier.padding(12.dp)) {
          RadioButton(selected = mode == option, onClick = null)
          Column(Modifier.padding(start = 12.dp)) {
           Text(option.title, style = MaterialTheme.typography.titleMedium)
           Text(option.explanation, style = MaterialTheme.typography.bodyMedium)
          }
         }
        }
       }
       if (mode == ProtectionMode.APPS) {
        Text("${availableSelected.size} apps selected", style = MaterialTheme.typography.titleMedium)
        Text("Suggested social apps are listed first. Review them, then add any others from the installed-app list. Essential system apps, calls and the launcher are excluded.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
         TextButton(onClick = { lifecycleScope.launch { settings.edit { it[Preferences.blockedApps] = selected + installedApps.filter { app -> app.suggested }.map { app -> app.packageName } } } }) { Text("Select social apps") }
         TextButton(onClick = { lifecycleScope.launch { settings.edit { it[Preferences.blockedApps] = emptySet(); it[Preferences.enabled] = false } } }) { Text("Clear") }
        }
        Row {
         Checkbox(showAllApps, onCheckedChange = { showAllApps = it })
         Text("Show other installed apps", Modifier.padding(top = 12.dp))
        }
        OutlinedTextField(search, onValueChange = { search = it }, label = { Text("Search apps") }, modifier = Modifier.fillMaxWidth())
        val visible = installedApps.filter { (showAllApps || it.suggested || it.packageName in selected) && it.label.contains(search, ignoreCase = true) }
        if (visible.isEmpty()) Text("No matching apps. Try showing other installed apps.")
        visible.forEach { app ->
         Row(Modifier.fillMaxWidth()) {
          Checkbox(app.packageName in selected, onCheckedChange = { checked -> lifecycleScope.launch {
           settings.edit { values ->
            val current = values[Preferences.blockedApps].orEmpty()
            val updated = if (checked) current + app.packageName else current - app.packageName
            values[Preferences.blockedApps] = updated
            if (updated.isEmpty()) values[Preferences.enabled] = false
           }
          } })
          Column(Modifier.padding(top = 8.dp)) {
           Text(app.label)
           Text(app.packageName, style = MaterialTheme.typography.labelSmall)
          }
         }
        }
        Text("Blocked apps get a full-screen cover with Go home and Turn protection off. Notifications, browser versions and other Android profiles are not blocked.")
       } else {
        Text("Instagram is included. For YouTube, opt in below to local inspection of titles and descriptions.")
        Row {
         Checkbox(prefs[Preferences.youtube] == true, onCheckedChange = { value -> lifecycleScope.launch { settings.edit { it[Preferences.youtube] = value } } })
         Text("Filter YouTube too", Modifier.padding(top = 12.dp))
        }
        Text("Text filtering is experimental. Unrecognized layouts, images and spoken spoilers may be missed. Content may flash before detection; Shield tries to pause a matched video only when its Pause control is exposed. Audio may continue otherwise. Reveal stays until you scroll, navigate away or the item changes. Covers are temporarily removed while scrolling.")
        if (mode == ProtectionMode.SPOILERS) Text("Broad phrases such as WHO DIES and SECRET ENDING can also match unrelated content.")
       }
       Button(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Open Accessibility settings") }
       val ready = accessibilityEnabled && (mode != ProtectionMode.APPS || availableSelected.isNotEmpty())
       Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enable protection", Modifier.padding(top = 12.dp))
        Switch(checked = prefs[Preferences.enabled] == true && ready, enabled = ready, onCheckedChange = { value -> lifecycleScope.launch { settings.edit { it[Preferences.enabled] = value } } })
       }
       Text(when {
        !accessibilityEnabled -> "Enable Spoiler Shield in Android Accessibility settings first."
        mode == ProtectionMode.APPS && availableSelected.isEmpty() -> "Select at least one app to block."
        prefs[Preferences.enabled] != true -> "Protection off"
        else -> health
       })
       TextButton(onClick = { lifecycleScope.launch { settings.edit { it[Preferences.enabled] = false; it[Preferences.consent] = false; it[Preferences.youtube] = false } } }) { Text("Revoke consent") }
      }
      Spacer(Modifier.height(24.dp))
     }
    }
   }
  }
 }
}
