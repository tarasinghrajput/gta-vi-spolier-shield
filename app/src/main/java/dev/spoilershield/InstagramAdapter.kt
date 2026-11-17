package dev.spoilershield
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class InstagramAdapter {
 fun inspect(root: AccessibilityNodeInfo, contentMatches: (String) -> Boolean = KeywordMatcher::matches): FilterResult {
  val snapshot = ContentSnapshot(root) { id -> listOf("direct", "login", "settings", "password").any(id::contains) }
  val viewport = Rect().also(root::getBoundsInScreen)
  val targets = mutableListOf<CoverTarget>()
  var supported = false
  fun walk(node: ContentSnapshot.Node) {
   if (node.id in setOf("row_feed_photo", "row_feed_video", "clips_viewer_view_pager")) {
    supported = true
    node.matchingText(contentMatches)?.let { text -> ContentSnapshot.target(node, text, viewport)?.let(targets::add) }
    return
   }
   node.children.forEach(::walk)
  }
  snapshot.tree?.let(::walk)
  return FilterResult(targets.distinctBy { it.key }, supported, snapshot.incomplete)
 }
}
