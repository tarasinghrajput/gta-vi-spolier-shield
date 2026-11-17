package dev.spoilershield
import org.junit.Assert.*
import org.junit.Test
class ProtectionPolicyTest {
 @Test fun distinguishesTopicFromSpoilers() {
  val title = "GTA VI will be my last game"
  assertTrue(ProtectionPolicy.matches(ProtectionMode.TOPIC, title))
  assertFalse(ProtectionPolicy.matches(ProtectionMode.SPOILERS, title))
  assertFalse(ProtectionPolicy.matches(ProtectionMode.APPS, title))
 }
 @Test fun topicAliasesAndBoundaries() {
  listOf("GTA VI", "gta 6 trailer", "#GTAVI", "Grand Theft Auto VI", "grand_theft_auto_6").forEach { assertTrue(it, TopicMatcher.matches(it)) }
  listOf("GTA V", "GTA VII", "GTA 60", "unrelated", "").forEach { assertFalse(it, TopicMatcher.matches(it)) }
 }
 @Test fun appBlockingRequiresEveryCondition() {
  assertTrue(ProtectionPolicy.blocksApp(true, ProtectionMode.APPS, setOf("social.app"), "social.app"))
  assertFalse(ProtectionPolicy.blocksApp(false, ProtectionMode.APPS, setOf("social.app"), "social.app"))
  assertFalse(ProtectionPolicy.blocksApp(true, ProtectionMode.SPOILERS, setOf("social.app"), "social.app"))
  assertFalse(ProtectionPolicy.blocksApp(true, ProtectionMode.APPS, emptySet(), "social.app"))
  assertFalse(ProtectionPolicy.blocksApp(true, ProtectionMode.APPS, setOf("social.app"), "other.app"))
 }
 @Test fun existingInstallsKeepSpoilerMode() {
  assertEquals(ProtectionMode.SPOILERS, ProtectionMode.fromStored(null))
  assertEquals(ProtectionMode.SPOILERS, ProtectionMode.fromStored("invalid"))
  assertEquals(ProtectionMode.TOPIC, ProtectionMode.fromStored("TOPIC"))
 }
}
