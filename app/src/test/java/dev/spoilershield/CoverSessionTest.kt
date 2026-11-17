package dev.spoilershield
import org.junit.Assert.*
import org.junit.Test
class CoverSessionTest {
 @Test fun revealSurvivesRepeatedUpdates() {
  val session = CoverSession()
  session.toggle("video-a")
  repeat(100) { session.retain(setOf("video-a")); assertTrue(session.isRevealed("video-a")) }
 }
 @Test fun revealDoesNotTransferToNextItem() {
  val session = CoverSession(); session.toggle("video-a")
  session.retain(setOf("video-b"))
  assertFalse(session.isRevealed("video-a")); assertFalse(session.isRevealed("video-b"))
 }
 @Test fun scrollOrNavigationResetsReveal() {
  val session = CoverSession(); session.toggle("video-a"); session.reset()
  assertFalse(session.isRevealed("video-a"))
 }
 @Test fun hideAgainReversesReveal() {
  val session = CoverSession(); session.toggle("video-a"); session.toggle("video-a")
  assertFalse(session.isRevealed("video-a"))
 }
 @Test fun frequentContentEventsDoNotStarveScan() {
  val timing = ScanTiming(); timing.started(1000)
  assertEquals(80L, timing.waitMillis(1020))
  assertEquals(0L, timing.waitMillis(1100))
  assertEquals(0L, timing.waitMillis(1200))
 }
 @Test fun scrollingWaitsForBriefQuietPeriod() {
  val timing = ScanTiming(); timing.scrolled(1000)
  assertEquals(140L, timing.waitMillis(1000))
  timing.scrolled(1100)
  assertEquals(40L, timing.waitMillis(1200))
  assertEquals(0L, timing.waitMillis(1240))
 }
 @Test fun fullAppBlockingDoesNotWaitForScroll() {
  val timing = ScanTiming(); timing.scrolled(1000)
  assertEquals(0L, timing.waitMillis(1000, fullApp = true))
 }
}
