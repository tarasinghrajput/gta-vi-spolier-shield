package dev.spoilershield

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.*

class ShieldService : AccessibilityService() {
 private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
 private lateinit var overlays: OverlayController
 private val instagram = InstagramAdapter()
 private val youtube = YouTubeAdapter()
 private val timing = ScanTiming()
 private var scanJob: Job? = null
 private var pending = false
 private var revision = 0L
 private var active = false
 private var youtubeEnabled = false
 private var mode = ProtectionMode.SPOILERS
 private var blockedApps = emptySet<String>()
 private var visiblePackage: String? = null
 private var hadTargets = false
 private var emptySince = 0L
 private data class Scan(val packageName: String, val result: FilterResult?)

 override fun onServiceConnected() {
  overlays = OverlayController(this)
  Health.status.value = "Connected • protection off"
  scope.launch {
   settings.data.collect { prefs ->
    active = false
    reset()
    mode = ProtectionMode.fromStored(prefs[Preferences.mode])
    youtubeEnabled = prefs[Preferences.youtube] == true
    val selected = prefs[Preferences.blockedApps].orEmpty()
    blockedApps = if (mode == ProtectionMode.APPS) {
     val allowed = withContext(Dispatchers.IO) { InstalledApps.load(this@ShieldService).map { it.packageName }.toSet() }
     selected.intersect(allowed)
    } else emptySet()
    active = prefs[Preferences.consent] == true && prefs[Preferences.enabled] == true && (mode != ProtectionMode.APPS || blockedApps.isNotEmpty())
    if (active) requestScan() else Health.status.value = "Protection off"
   }
  }
 }
 override fun onAccessibilityEvent(event: AccessibilityEvent?) {
  if (!::overlays.isInitialized || event == null || !active) return
  val eventPackage = event.packageName?.toString()
  if (eventPackage == packageName || event.eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) return
  if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && eventPackage != null && eventPackage != visiblePackage) {
   reset()
   visiblePackage = eventPackage
  }
  if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED && mode != ProtectionMode.APPS) {
   revision++
   overlays.clear()
   hadTargets = false; emptySince = 0
   timing.scrolled(SystemClock.uptimeMillis())
  }
  requestScan()
 }
 private fun requestScan() {
  pending = true
  if (scanJob?.isActive == true) return
  scanJob = scope.launch {
   try {
    while (pending && active) {
     var wait = timing.waitMillis(SystemClock.uptimeMillis(), mode == ProtectionMode.APPS)
     while (wait > 0) { delay(wait); wait = timing.waitMillis(SystemClock.uptimeMillis(), mode == ProtectionMode.APPS) }
     pending = false
     val currentRevision = revision
     val currentMode = mode
     val youtubeAllowed = youtubeEnabled
     timing.started(SystemClock.uptimeMillis())
     val scan = withContext(Dispatchers.Default) {
      val root = rootInActiveWindow
      if (root == null) null else {
       val app = root.packageName?.toString().orEmpty()
       val matcher: (String) -> Boolean = { ProtectionPolicy.matches(currentMode, it) }
       val result = if (currentMode == ProtectionMode.APPS) null else when (app) {
        "com.instagram.android" -> instagram.inspect(root, matcher)
        "com.google.android.youtube" -> if (youtubeAllowed) youtube.inspect(root, matcher) else null
        else -> null
       }
       Scan(app, result)
      }
     }
     ensureActive()
     if (currentRevision != revision || !active) continue
     render(scan)
    }
   } finally { if (scanJob == currentCoroutineContext()[Job]) scanJob = null }
  }
 }
 private suspend fun render(scan: Scan?) {
  if (scan == null) { overlays.clear(); hadTargets = false; Health.status.value = "Waiting for a visible app"; return }
  if (visiblePackage != scan.packageName) {
   overlays.clear(); hadTargets = false; emptySince = 0; visiblePackage = scan.packageName
  }
  if (mode == ProtectionMode.APPS) {
   Health.status.value = "App blocking active • ${blockedApps.size} selected"
   if (ProtectionPolicy.blocksApp(active, mode, blockedApps, scan.packageName)) {
    overlays.blockApp(scan.packageName,
     onHome = { performGlobalAction(GLOBAL_ACTION_HOME); overlays.clear() },
     onDisable = { active = false; reset(); scope.launch { settings.edit { it[Preferences.enabled] = false } } }
    )
   } else overlays.clear()
   return
  }
  val result = scan.result
  if (result == null) { overlays.clear(); hadTargets = false; Health.status.value = "Waiting for Instagram${if (youtubeEnabled) " or YouTube" else ""}"; return }
  // Tolerate one short, incomplete tree update rather than flashing/removing an existing cover.
  if (result.targets.isEmpty() && hadTargets) {
   val now = SystemClock.uptimeMillis()
   if (emptySince == 0L) emptySince = now
   if (now - emptySince < 160) { pending = true; delay(160 - (now - emptySince)); return }
  } else emptySince = 0
  hadTargets = result.targets.isNotEmpty()
  Health.status.value = when {
   result.incomplete -> "Partial view • some content may not be checked"
   result.supported -> "Filtering recognized view • ${mode.title}"
   else -> "Unable to filter this view"
  }
  overlays.show(result.targets, if (mode == ProtectionMode.TOPIC) "GTA VI content hidden" else "Potential GTA VI spoiler")
 }
 private fun reset() {
  revision++
  scanJob?.cancel(); scanJob = null; pending = false
  timing.reset(); visiblePackage = null; hadTargets = false; emptySince = 0
  if (::overlays.isInitialized) overlays.clear()
 }
 override fun onInterrupt() { active = false; reset(); Health.status.value = "Service interrupted" }
 override fun onDestroy() { onInterrupt(); scope.cancel(); Health.status.value = "Accessibility service disconnected"; super.onDestroy() }
}
