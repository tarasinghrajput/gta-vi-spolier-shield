package dev.spoilershield
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

class YouTubeAdapter {
 fun inspect(root: AccessibilityNodeInfo, contentMatches: (String) -> Boolean = KeywordMatcher::matches): FilterResult {
  val screen = Rect().also(root::getBoundsInScreen)
  val snapshot = ContentSnapshot(root) { id -> listOf("search_edit", "comment", "account", "settings").any(id::contains) }
  val targets = mutableListOf<CoverTarget>()
  var supported = false
  fun walk(node: ContentSnapshot.Node) {
   val bounds = node.bounds
   val candidate = node.id == "reel_player_page_container" ||
    (node.clickable && node.description.isNotBlank() && bounds.width() >= screen.width() / 2 && bounds.height() >= 120 && bounds.height() <= screen.height() * 0.7 && bounds != screen)
   if (candidate) {
    supported = true
    val text = node.matchingText(contentMatches)
    if (text != null) {
     ContentSnapshot.target(node, text, screen)?.let(targets::add)
     return
    }
   }
   node.children.forEach(::walk)
  }
  snapshot.tree?.let(::walk)
  return FilterResult(targets.distinctBy { it.key }, supported, snapshot.incomplete)
 }
}
