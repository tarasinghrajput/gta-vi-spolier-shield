package dev.spoilershield

import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import java.security.MessageDigest

data class CoverTarget(val key: String, val bounds: Rect, val pauseControl: AccessibilityNodeInfo? = null)
data class FilterResult(val targets: List<CoverTarget>, val supported: Boolean, val incomplete: Boolean = false)

/** One bounded traversal per scan. Raw text and nodes never leave this transient snapshot. */
internal class ContentSnapshot(root: AccessibilityNodeInfo, private val excluded: (String) -> Boolean) {
 data class Node(val source: AccessibilityNodeInfo, val id: String, val bounds: Rect, val text: String, val description: String, val clickable: Boolean, val children: List<Node>) {
  fun matchingText(matches: (String) -> Boolean): String? {
   if (text.isNotBlank() && matches(text)) return text
   // Prefer a child title/caption to a card description containing changing counters.
   children.forEach { it.matchingText(matches)?.let { match -> return match } }
   return description.takeIf { it.isNotBlank() && matches(it) }
  }
  fun pauseControl(): AccessibilityNodeInfo? {
   if (clickable && (description.trim().lowercase() in setOf("pause", "pause video") || text.trim().lowercase() == "pause")) return source
   return children.firstNotNullOfOrNull { it.pauseControl() }
  }
 }
 private val deadline = SystemClock.uptimeMillis() + 120
 private var count = 0
 var incomplete = false
  private set
 val tree: Node? = read(root, 0)
 private fun read(node: AccessibilityNodeInfo, depth: Int): Node? {
  if (++count > 350 || depth > 30 || SystemClock.uptimeMillis() > deadline) { incomplete = true; return null }
  if (!node.isVisibleToUser || node.isEditable) return null
  val id = node.viewIdResourceName?.substringAfter(":id/").orEmpty()
  if (excluded(id)) return null
  val children = mutableListOf<Node>()
  for (i in 0 until minOf(node.childCount, 100)) {
   if (SystemClock.uptimeMillis() > deadline) { incomplete = true; break }
   node.getChild(i)?.let { read(it, depth + 1) }?.let(children::add)
  }
  return Node(node, id, Rect().also(node::getBoundsInScreen), node.text?.toString().orEmpty(), node.contentDescription?.toString().orEmpty(), node.isClickable, children)
 }
 companion object {
  fun target(node: Node, text: String, viewport: Rect): CoverTarget? {
   val bounds = Rect(node.bounds)
   if (!bounds.intersect(viewport) || bounds.isEmpty) return null
   val stable = text.lowercase().replace(Regex("\\b\\d+(?:[.,]\\d+)?\\s*(?:views?|likes?|comments?|seconds?|minutes?|hours?|days?|weeks?|months?|years?)(?:\\s+ago)?\\b"), "")
    .replace(Regex("\\b\\d{1,2}:\\d{2}(?::\\d{2})?\\b"), "").replace(Regex("\\s+"), " ").trim()
   val key = MessageDigest.getInstance("SHA-256").digest((node.id + ":" + stable).toByteArray()).joinToString("") { "%02x".format(it) }
   return CoverTarget(key, bounds, node.pauseControl())
  }
 }
}
