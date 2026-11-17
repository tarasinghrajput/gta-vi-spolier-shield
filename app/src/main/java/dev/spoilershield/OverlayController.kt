package dev.spoilershield

import android.content.Context
import android.graphics.PixelFormat
import android.view.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*

class OverlayController(private val ctx: Context) {
 private val manager = ctx.getSystemService(WindowManager::class.java)
 private val session = CoverSession()
 private data class Entry(val panel: LinearLayout, val label: TextView, val button: Button, val params: WindowManager.LayoutParams, var target: CoverTarget, var paused: Boolean = false)
 private val entries = mutableMapOf<String, Entry>()
 private var appPanel: View? = null
 private var blockedPackage: String? = null
 fun clear() {
  entries.values.forEach { runCatching { manager.removeViewImmediate(it.panel) } }; entries.clear()
  appPanel?.let { runCatching { manager.removeViewImmediate(it) } }; appPanel = null; blockedPackage = null
  session.reset()
 }
 fun blockApp(packageName: String, onHome: () -> Unit, onDisable: () -> Unit) {
  if (blockedPackage == packageName && appPanel != null) return
  clear()
  val panel = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(0xff191a24.toInt()); setPadding(32, 32, 32, 32) }
  panel.addView(TextView(ctx).apply { text = "This app is blocked\nKeep your story spoiler-free."; setTextColor(-1); textSize = 22f; gravity = Gravity.CENTER })
  panel.addView(Button(ctx).apply { text = "Go home"; setOnClickListener { onHome() } })
  panel.addView(Button(ctx).apply { text = "Turn protection off"; setOnClickListener { onDisable() } })
  val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
  runCatching { manager.addView(panel, params); appPanel = panel; blockedPackage = packageName }
 }
 fun show(targets: List<CoverTarget>, warning: String) {
  if (appPanel != null) clear()
  val keys = targets.map { it.key }.toSet()
  session.retain(keys)
  entries.keys.filter { it !in keys }.forEach { key -> entries.remove(key)?.let { runCatching { manager.removeViewImmediate(it.panel) } } }
  targets.forEach { target ->
   val existing = entries[target.key]
   if (existing != null) {
    val moved = existing.target.bounds != target.bounds
    existing.target = target
    if (existing.label.text.toString() != warning) existing.label.text = warning
    if (moved) update(existing)
    pauseIfAvailable(existing)
   } else {
    val panel = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setBackgroundColor(0xff191a24.toInt()) }
    val label = TextView(ctx).apply { text = warning; setTextColor(-1); textSize = 18f; gravity = Gravity.CENTER }
    val button = Button(ctx).apply { text = "Reveal" }
    panel.addView(label); panel.addView(button)
    val bounds = target.bounds
    val params = WindowManager.LayoutParams(bounds.width(), bounds.height(), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.LEFT; x = bounds.left; y = bounds.top }
    val entry = Entry(panel, label, button, params, target)
    button.setOnClickListener { session.toggle(target.key); update(entry); if (!session.isRevealed(target.key)) { entry.paused = false; pauseIfAvailable(entry) } }
    runCatching { manager.addView(panel, params); entries[target.key] = entry; pauseIfAvailable(entry) }
   }
  }
 }
 private fun update(entry: Entry) {
  val revealed = session.isRevealed(entry.target.key)
  entry.label.visibility = if (revealed) View.GONE else View.VISIBLE
  entry.button.text = if (revealed) "Hide again" else "Reveal"
  entry.params.apply { x = entry.target.bounds.left; y = entry.target.bounds.top; width = entry.target.bounds.width(); height = if (revealed) WindowManager.LayoutParams.WRAP_CONTENT else entry.target.bounds.height() }
  runCatching { manager.updateViewLayout(entry.panel, entry.params) }
 }
 private fun pauseIfAvailable(entry: Entry) {
  if (entry.paused || session.isRevealed(entry.target.key)) return
  // Only a Pause control from this matched card, never a device-wide media key.
  val control = entry.target.pauseControl ?: return
  entry.paused = runCatching { control.performAction(AccessibilityNodeInfo.ACTION_CLICK) }.getOrDefault(false)
 }
}
